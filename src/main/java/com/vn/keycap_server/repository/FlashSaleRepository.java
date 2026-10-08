package com.vn.keycap_server.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.utils.EFlashSaleStatus;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long>, JpaSpecificationExecutor<FlashSale> {

  Page<FlashSale> findAllByStatus(EFlashSaleStatus status, Pageable pageable);

  @Query("""
      SELECT s FROM FlashSale s
      WHERE s.status = :status
        AND s.startTime <= :time
        AND s.endTime >= :time
      """)
  List<FlashSale> findActiveSales(@Param("status") EFlashSaleStatus status, @Param("time") LocalDateTime time);

  @Query("""
      SELECT s FROM FlashSale s
      WHERE s.status = 'UPCOMING'
        AND s.startTime <= :time
      """)
  List<FlashSale> findUpcomingSalesReadyToActivate(@Param("time") LocalDateTime time);

  @Query("""
      SELECT s FROM FlashSale s
      WHERE s.status = 'ACTIVE'
        AND s.endTime < :time
      """)
  List<FlashSale> findActiveSalesReadyToExpire(@Param("time") LocalDateTime time);
}
