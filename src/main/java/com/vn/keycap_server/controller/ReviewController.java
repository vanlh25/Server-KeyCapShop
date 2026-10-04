package com.vn.keycap_server.controller;

import com.vn.keycap_server.dto.request.review.CreateReviewRequest;
import com.vn.keycap_server.dto.request.review.UpdateReviewRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.PaginationMeta;
import com.vn.keycap_server.dto.response.review.ReviewResponse;
import com.vn.keycap_server.dto.response.review.AvailableReviewResponse;
import com.vn.keycap_server.service.review.IReviewService;
import java.util.List;
import com.vn.keycap_server.utils.PaginationUtils;
import com.vn.keycap_server.utils.JwtUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    /**
     * API GET /reviews lấy danh sách đánh giá của sản phẩm theo phân trang.
     * 
     * @param productId ID của sản phẩm cần lấy đánh giá
     * @param page      Số trang hiện tại (mặc định = 1)
     * @param pageSize  Kích thước trang (mặc định = 10)
     * @return ResponseEntity chứa danh sách ReviewResponse cùng metadata phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getReviewsByProductId(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<ReviewResponse> resultPage = reviewService.getReviewsByProductId(productId, page, pageSize);

        PaginationMeta meta = PaginationUtils.buildPaginationMeta(resultPage, page);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách đánh giá thành công")
                .data(resultPage.getContent())
                .pagination(meta)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createReviews(
            @RequestBody @Valid CreateReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtUtils.getUserId(jwt);
        reviewService.createReviews(request, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Gửi đánh giá sản phẩm thành công!")
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateReview(
            @PathVariable Long id,
            @RequestBody @Valid UpdateReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtUtils.getUserId(jwt);
        reviewService.updateReview(id, request, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật đánh giá thành công!")
                .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAvailableReviews(
            @RequestParam Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtUtils.getUserId(jwt);
        List<AvailableReviewResponse> data = reviewService.getAvailableReviews(orderId, userId);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách đánh giá có sẵn thành công")
                .data(data)
                .build());
    }
}
