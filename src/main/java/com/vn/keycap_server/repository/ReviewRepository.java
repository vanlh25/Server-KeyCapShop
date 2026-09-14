package com.vn.keycap_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.Review;
import com.vn.keycap_server.repository.projection.ProductRatingSummaryProjection;
import com.vn.keycap_server.repository.projection.ProductReviewSummaryProjection;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isHidden = false")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Lấy đánh giá công khai của sản phẩm — CHỈ review không bị ẩn (isHidden = false).
     * Dùng cho trang sản phẩm phía client.
     */
    @EntityGraph(attributePaths = { "user", "user.avatarMedia", "reply", "reply.user" })
    Page<Review> findByProduct_IdAndIsHiddenFalse(Long productId, Pageable pageable);

    /**
     * Gom điểm đánh giá trung bình theo danh sách productId.
     * Chỉ tính review không bị ẩn để đảm bảo điểm chính xác.
     *
     * @param productIds danh sách ID sản phẩm trong trang hiện tại
     * @return danh sách projection rating theo sản phẩm
     */
    @Query("""
            SELECT r.product.id AS productId,
                   AVG(r.rating) AS rating
            FROM Review r
            WHERE r.product.id IN :productIds AND r.isHidden = false
            GROUP BY r.product.id
            """)
    List<ProductRatingSummaryProjection> findRatingSummariesByProductIds(@Param("productIds") List<Long> productIds);

    // Tìm các review của một đơn hàng
    List<Review> findByOrder_Id(Long orderId);

    // Kiểm tra sản phẩm trong đơn hàng đã được đánh giá chưa
    boolean existsByOrder_IdAndProduct_Id(Long orderId, Long productId);

    /**
     * Lấy danh sách sản phẩm có đánh giá kèm thống kê, dùng cho trang admin quản lý đánh giá.
     * Lấy thumbnail từ ảnh primary đầu tiên của sản phẩm.
     */
    @Query("""
            SELECT
                r.product.id                            AS productId,
                r.product.name                          AS productName,
                (SELECT pi.url FROM ProductImage pi
                 WHERE pi.product.id = r.product.id AND pi.primary = true
                 ORDER BY pi.id ASC
                 LIMIT 1)                               AS productThumbnail,
                COUNT(r.id)                             AS totalReviews,
                SUM(CASE WHEN r.isHidden = true THEN 1 ELSE 0 END) AS hiddenCount,
                AVG(r.rating)                           AS averageRating
            FROM Review r
            GROUP BY r.product.id, r.product.name
            ORDER BY COUNT(r.id) DESC
            """)
    Page<ProductReviewSummaryProjection> findProductReviewSummaries(Pageable pageable);

    /**
     * Lấy tất cả đánh giá của một sản phẩm (kể cả đang ẩn) cho admin.
     */
    @EntityGraph(attributePaths = { "user", "user.avatarMedia", "reply", "reply.user" })
    Page<Review> findAllByProduct_Id(Long productId, Pageable pageable);
}
