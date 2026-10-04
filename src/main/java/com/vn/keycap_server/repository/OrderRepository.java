package com.vn.keycap_server.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.Order;
import com.vn.keycap_server.utils.EOrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, EOrderStatus status);

    boolean existsByUserId(Long userId);

    Optional<Order> findByTransactionId(String transactionId);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, EOrderStatus status);

    List<Order> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<EOrderStatus> statuses);

    Page<Order> findByStatus(EOrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN o.user u " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (CAST(o.id AS string) LIKE %:keyword% " +
            "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> searchOrders(@Param("keyword") String keyword, @Param("status") EOrderStatus status, Pageable pageable);

    // Tổng doanh thu đơn hàng SUCCESS
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'SUCCESS'")
    BigDecimal calculateTotalRevenue();

    // Tổng doanh thu theo ngày
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'SUCCESS' AND o.createdAt =:today")
    BigDecimal calculateTodayRevenue(@Param("today") LocalDate today);

    // Tổng doanh thu theo khoảng thoi gian
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'SUCCESS' " +
            "AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    // Total Order
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt =:today")
    Long countTodayOrders(@Param("today") LocalDate today);

    Long countByStatus(EOrderStatus status);

    // Count Status Order
    @Query("SELECT o.status, count(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    // Revenue Chart
    @Query(value = "SELECT o.created_at AS date, COALESCE(SUM(o.total_amount), 0) AS revenue " +
            "FROM orders o " +
            "WHERE o.status = 'SUCCESS' AND o.created_at >= :startDate " +
            "GROUP BY o.created_at " +
            "ORDER BY o.created_at ASC", nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDate startDate);

    // Top User
    @Query("SELECT o.user.id, o.user.fullName, o.user.email, " +
            "COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
            "FROM Order o " +
            "WHERE o.status = 'SUCCESS' " +
            "GROUP BY o.user.id, o.user.fullName, o.user.email " +
            "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomers(Pageable pageable);

    // Tổng chi tiêu đơn hàng SUCCESS của một khách hàng (dùng cho Customer Management)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = 'SUCCESS'")
    BigDecimal sumTotalSpentByUserId(@Param("userId") Long userId);
}

