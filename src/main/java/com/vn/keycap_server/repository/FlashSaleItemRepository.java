package com.vn.keycap_server.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.FlashSaleItem;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    List<FlashSaleItem> findByFlashSaleId(Long flashSaleId);

    Optional<FlashSaleItem> findByFlashSaleIdAndId(Long flashSaleId, Long id);

    @Query("""
            SELECT COUNT(i) > 0 FROM FlashSaleItem i
            JOIN i.flashSale s
            WHERE i.variant.id = :variantId
              AND s.status IN (com.vn.keycap_server.utils.EFlashSaleStatus.UPCOMING, com.vn.keycap_server.utils.EFlashSaleStatus.ACTIVE)
              AND (:excludeSaleId IS NULL OR s.id <> :excludeSaleId)
              AND s.startTime < :endTime AND s.endTime > :startTime
            """)
    boolean existsOverlappingSaleForVariant(@Param("variantId") Long variantId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("excludeSaleId") Long excludeSaleId);

    @Query("""
            SELECT i FROM FlashSaleItem i
            JOIN i.flashSale s
            WHERE i.variant.id = :variantId
              AND s.status = 'ACTIVE'
              AND s.startTime <= :time
              AND s.endTime >= :time
            ORDER BY s.endTime ASC, s.id DESC
            """)
    List<FlashSaleItem> findActiveItemsByVariantId(@Param("variantId") Long variantId, @Param("time") LocalDateTime time);

    default Optional<FlashSaleItem> findActiveItemByVariantId(Long variantId, LocalDateTime time) {
        List<FlashSaleItem> items = findActiveItemsByVariantId(variantId, time);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    @org.springframework.data.jpa.repository.Modifying
    @Query("""
            UPDATE FlashSaleItem i
            SET i.soldSlots = i.soldSlots + :qty
            WHERE i.id = :itemId AND i.soldSlots + :qty <= i.totalSlots
            """)
    int incrementSoldSlots(@Param("itemId") Long itemId, @Param("qty") int qty);
}
