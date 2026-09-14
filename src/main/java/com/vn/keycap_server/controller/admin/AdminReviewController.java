package com.vn.keycap_server.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.PaginationMeta;
import com.vn.keycap_server.dto.request.review.CreateReplyRequest;
import com.vn.keycap_server.dto.response.review.AdminReviewSummaryResponse;
import com.vn.keycap_server.dto.response.review.ReviewResponse;
import com.vn.keycap_server.service.review.IReviewService;
import com.vn.keycap_server.utils.PaginationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.vn.keycap_server.utils.JwtUtils;

@Validated
@RestController
@RequestMapping("/admin/reviews")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class AdminReviewController {

    private final IReviewService reviewService;

    /**
     * GET /admin/reviews/products
     * Lấy danh sách sản phẩm có đánh giá kèm thống kê (tổng, ẩn, trung bình sao).
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProductReviewSummaries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<AdminReviewSummaryResponse> resultPage = reviewService.getProductReviewSummaries(page, pageSize);
        PaginationMeta meta = PaginationUtils.buildPaginationMeta(resultPage, page);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách sản phẩm có đánh giá thành công")
                .data(resultPage.getContent())
                .pagination(meta)
                .build());
    }

    /**
     * GET /admin/reviews?productId=&page=&pageSize=
     * Lấy tất cả đánh giá (kể cả ẩn) của một sản phẩm.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAdminReviewsByProduct(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<ReviewResponse> resultPage = reviewService.getAdminReviewsByProductId(productId, page, pageSize);
        PaginationMeta meta = PaginationUtils.buildPaginationMeta(resultPage, page);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách đánh giá thành công")
                .data(resultPage.getContent())
                .pagination(meta)
                .build());
    }

    /**
     * PATCH /admin/reviews/{reviewId}/toggle
     * Ẩn hoặc hiện một đánh giá.
     */
    @PatchMapping("/{reviewId}/toggle")
    public ResponseEntity<ApiResponse> toggleReviewVisibility(@PathVariable Long reviewId) {
        reviewService.toggleReviewVisibility(reviewId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật trạng thái đánh giá thành công")
                .build());
    }

    /**
     * POST /admin/reviews/{reviewId}/reply
     * Phản hồi đánh giá của khách hàng.
     */
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse> replyToReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid CreateReplyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtUtils.getUserId(jwt);
        reviewService.replyToReview(reviewId, request, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Gửi phản hồi thành công!")
                .build());
    }
}
