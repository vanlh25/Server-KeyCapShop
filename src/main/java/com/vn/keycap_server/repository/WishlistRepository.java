package com.vn.keycap_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.Wishlist;

/**
 * WishlistRepository quản lý các truy vấn với bảng 'wishlists'.
 * Hỗ trợ kiểm tra sản phẩm yêu thích của người dùng.
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    long countByUserId(Long userId);

    /**
     * Lấy danh sách sản phẩm yêu thích kèm phân trang cho user.
     */
    @Query(value = "select w.product from Wishlist w where w.user.id = :userId order by w.id desc",
           countQuery = "select count(w) from Wishlist w where w.user.id = :userId")
    Page<Product> findWishlistProductsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Lấy danh sách ID sản phẩm yêu thích của user.
     *
     * @param userId ID của user
     * @return danh sách productId đã được user yêu thích
     */
    @Query("select w.product.id from Wishlist w where w.user.id = :userId")
    List<Long> findFavoriteProductIds(@Param("userId") Long userId);

    // Find Wishlist by productId and userId
    @Query("select w from Wishlist w where w.product.id = :productId and w.user.id = :userId")
    Optional<Wishlist> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    // Delete Wishlist by productId and userId
    @Modifying
    @Transactional
    @Query("delete from Wishlist w where w.product.id = :productId and w.user.id = :userId")
    void deleteByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    /**
     * Xóa toàn bộ wishlist đang tham chiếu đến một sản phẩm.
     *
     * @param productId ID sản phẩm cần dọn khỏi wishlist
     */
    @Modifying
    @Query("delete from Wishlist w where w.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
