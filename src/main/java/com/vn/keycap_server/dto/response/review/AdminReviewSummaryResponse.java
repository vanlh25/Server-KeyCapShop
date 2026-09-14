package com.vn.keycap_server.dto.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response trả về thống kê đánh giá theo từng sản phẩm, dùng cho trang admin quản lý đánh giá.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewSummaryResponse {
    private Long productId;
    private String productName;
    private String productThumbnail;
    private Long totalReviews;
    private Long hiddenCount;
    private Long visibleCount;
    private Double averageRating;
}
