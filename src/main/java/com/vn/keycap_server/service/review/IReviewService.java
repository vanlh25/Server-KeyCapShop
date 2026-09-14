package com.vn.keycap_server.service.review;

import com.vn.keycap_server.dto.request.review.CreateReplyRequest;
import com.vn.keycap_server.dto.request.review.CreateReviewRequest;
import org.springframework.data.domain.Page;

import com.vn.keycap_server.dto.response.review.ReviewResponse;
import com.vn.keycap_server.dto.response.review.AvailableReviewResponse;
import com.vn.keycap_server.dto.response.review.AdminReviewSummaryResponse;
import java.util.List;

public interface IReviewService {
    Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int pageSize);

    void createReviews(CreateReviewRequest request, Long userId);

    void replyToReview(Long reviewId, CreateReplyRequest request, Long userId);

    List<AvailableReviewResponse> getAvailableReviews(Long orderId, Long userId);

    /** Lấy danh sách sản phẩm có đánh giá kèm thống kê (dành cho admin). */
    Page<AdminReviewSummaryResponse> getProductReviewSummaries(int page, int pageSize);

    /** Lấy tất cả đánh giá (kể cả ẩn) của một sản phẩm (dành cho admin). */
    Page<ReviewResponse> getAdminReviewsByProductId(Long productId, int page, int pageSize);

    /** Ẩn hoặc hiện một đánh giá (toggle). */
    void toggleReviewVisibility(Long reviewId);
}
