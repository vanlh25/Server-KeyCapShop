package com.vn.keycap_server.repository.projection;

/**
 * Projection gom số lượng đánh giá theo từng sản phẩm.
 * Dùng cho trang admin quản lý đánh giá để tránh query N+1.
 */
public interface ProductReviewSummaryProjection {

    /** ID sản phẩm */
    Long getProductId();

    /** Tên sản phẩm */
    String getProductName();

    /** URL ảnh thumbnail của sản phẩm */
    String getProductThumbnail();

    /** Tổng số đánh giá */
    Long getTotalReviews();

    /** Số đánh giá đang bị ẩn */
    Long getHiddenCount();

    /** Điểm trung bình */
    Double getAverageRating();
}
