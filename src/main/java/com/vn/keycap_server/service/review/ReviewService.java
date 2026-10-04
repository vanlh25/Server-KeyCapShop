package com.vn.keycap_server.service.review;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vn.keycap_server.dto.request.review.CreateReplyRequest;
import com.vn.keycap_server.dto.request.review.CreateReviewRequest;
import com.vn.keycap_server.dto.request.review.UpdateReviewRequest;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.modal.Order;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.repository.OrderRepository;
import com.vn.keycap_server.repository.ProductRepository;
import com.vn.keycap_server.utils.EOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.dto.response.review.ReviewResponse;
import com.vn.keycap_server.dto.response.review.AvailableReviewResponse;
import com.vn.keycap_server.dto.response.review.AdminReviewSummaryResponse;
import com.vn.keycap_server.mapper.ReviewMapper;
import com.vn.keycap_server.modal.Review;
import com.vn.keycap_server.modal.ReviewReply;
import com.vn.keycap_server.modal.User;
import com.vn.keycap_server.repository.ReviewReplyRepository;
import com.vn.keycap_server.repository.UserRepository;
import com.vn.keycap_server.repository.ReviewRepository;
import com.vn.keycap_server.repository.projection.ProductReviewSummaryProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;

    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int pageSize) {
        // Chuẩn hóa trang bắt đầu từ 0 cho Spring Data JPA
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, pageSize);

        // Đánh giá mới nhất hiển thị trước
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("createdAt").descending());

        Page<Review> reviewPage = reviewRepository.findByProduct_IdAndIsHiddenFalse(productId, pageable);

        List<ReviewResponse> responses = reviewPage.getContent().stream()
                .map(reviewMapper::reviewToReviewResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, reviewPage.getTotalElements());
    }

    @Override
    @Transactional
    public void createReviews(CreateReviewRequest request, Long userId) {
        // Tìm đơn hàng
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền đánh giá đơn hàng này");
        }

        // Đơn hàng phải SUCCESS mới được đánh giá
        if (order.getStatus() != EOrderStatus.SUCCESS) {
            throw new BadRequestException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
        }

        // Lấy danh sách productId trong đơn hàng
        Set<Long> productIdsInOrder = order.getItems().stream()
                .map(item -> item.getVariant().getProduct().getId())
                .collect(Collectors.toSet());

        List<Review> reviewsToSave = new ArrayList<>();

        for (CreateReviewRequest.ReviewItemRequest item : request.getReviews()) {
            // Kiểm tra sản phẩm có trong đơn hàng không
            if (!productIdsInOrder.contains(item.getProductId())) {
                throw new BadRequestException(
                        "Sản phẩm ID " + item.getProductId() + " không nằm trong đơn hàng này");
            }

            // Kiểm tra đã đánh giá trùng chưa
            if (reviewRepository.existsByOrder_IdAndProduct_Id(order.getId(), item.getProductId())) {
                throw new BadRequestException(
                        "Sản phẩm ID " + item.getProductId() + " đã được đánh giá trong đơn hàng này");
            }

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy sản phẩm"));

            Review review = Review.builder()
                    .order(order)
                    .product(product)
                    .user(order.getUser())
                    .rating(item.getRating())
                    .content(item.getContent())
                    .imageUrls(item.getImageUrls())
                    .build();

            reviewsToSave.add(review);
        }

        reviewRepository.saveAll(reviewsToSave);
    }

    @Override
    @Transactional
    public void replyToReview(Long reviewId, CreateReplyRequest request,Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đánh giá"));

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản ADMIN or STAFF"));

        if (review.getReply() != null) {
            ReviewReply existingReply = review.getReply();
            existingReply.setContent(request.getContent());
            existingReply.setUser(admin);
            reviewReplyRepository.save(existingReply);
        } else {
            ReviewReply reply = ReviewReply.builder()
                    .review(review)
                    .user(admin)
                    .content(request.getContent())
                    .build();
            reviewReplyRepository.save(reply);
        }
    }

    public static final int MAX_EDIT_DAYS = 30;

    @Override
    @Transactional
    public void updateReview(Long reviewId, UpdateReviewRequest request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đánh giá"));

        if (review.getUser() == null || !review.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        if (Boolean.TRUE.equals(review.getIsHidden())) {
            throw new BadRequestException("Đánh giá này đã bị ẩn bởi quản trị viên và không thể chỉnh sửa");
        }

        LocalDate now = LocalDate.now();
        LocalDate createdAt = review.getCreatedAt() != null ? review.getCreatedAt() : now;
        long daysPassed = ChronoUnit.DAYS.between(createdAt, now);
        if (daysPassed > MAX_EDIT_DAYS) {
            throw new BadRequestException("Đã quá thời hạn " + MAX_EDIT_DAYS + " ngày để chỉnh sửa đánh giá này");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());
        if (request.getImageUrls() != null) {
            review.setImageUrls(new ArrayList<>(request.getImageUrls()));
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableReviewResponse> getAvailableReviews(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xem đánh giá của đơn hàng này");
        }

        List<Review> reviews = reviewRepository.findByOrder_Id(orderId);
        LocalDate now = LocalDate.now();

        return reviews.stream()
                .map(r -> {
                    LocalDate createdAt = r.getCreatedAt() != null ? r.getCreatedAt() : now;
                    long daysPassed = ChronoUnit.DAYS.between(createdAt, now);
                    long remainingDays = Math.max(0, MAX_EDIT_DAYS - daysPassed);
                    boolean isHidden = Boolean.TRUE.equals(r.getIsHidden());
                    boolean canEdit = !isHidden && daysPassed <= MAX_EDIT_DAYS;

                    return AvailableReviewResponse.builder()
                            .id(r.getId())
                            .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                            .rating(r.getRating())
                            .content(r.getContent())
                            .imageUrls(r.getImageUrls() != null ? new ArrayList<>(r.getImageUrls()) : new ArrayList<>())
                            .createdAt(r.getCreatedAt())
                            .updatedAt(r.getUpdatedAt())
                            .canEdit(canEdit)
                            .remainingDays(remainingDays)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewSummaryResponse> getProductReviewSummaries(int page, int pageSize) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<ProductReviewSummaryProjection> resultPage = reviewRepository.findProductReviewSummaries(pageable);

        List<AdminReviewSummaryResponse> responses = resultPage.getContent().stream()
                .map(p -> AdminReviewSummaryResponse.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .productThumbnail(p.getProductThumbnail())
                        .totalReviews(p.getTotalReviews())
                        .hiddenCount(p.getHiddenCount())
                        .visibleCount(p.getTotalReviews() - (p.getHiddenCount() != null ? p.getHiddenCount() : 0L))
                        .averageRating(p.getAverageRating())
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, resultPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAdminReviewsByProductId(Long productId, int page, int pageSize) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("createdAt").descending());

        Page<Review> reviewPage = reviewRepository.findAllByProduct_Id(productId, pageable);

        List<ReviewResponse> responses = reviewPage.getContent().stream()
                .map(reviewMapper::reviewToReviewResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, reviewPage.getTotalElements());
    }

    @Override
    @Transactional
    public void toggleReviewVisibility(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đánh giá"));
        review.setIsHidden(!Boolean.TRUE.equals(review.getIsHidden()));
        reviewRepository.save(review);
    }
}
