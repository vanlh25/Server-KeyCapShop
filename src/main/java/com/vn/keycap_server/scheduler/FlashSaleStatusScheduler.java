package com.vn.keycap_server.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.repository.FlashSaleRepository;
import com.vn.keycap_server.utils.EFlashSaleStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tự động đồng bộ trạng thái chiến dịch Flash Sale theo thời gian thực:
 * - Kích hoạt UPCOMING -> ACTIVE khi chạm khung giờ bắt đầu
 * - Chuyển ACTIVE -> EXPIRED khi vượt quá khung giờ kết thúc
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FlashSaleStatusScheduler {

    private final FlashSaleRepository flashSaleRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 1 phút
    @Transactional
    public void syncFlashSaleStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kích hoạt các chiến dịch UPCOMING đã đến giờ
        List<FlashSale> toActivate = flashSaleRepository.findUpcomingSalesReadyToActivate(now);
        for (FlashSale sale : toActivate) {
            if (sale.getEndTime().isAfter(now)) {
                sale.setStatus(EFlashSaleStatus.ACTIVE);
                log.info("Flash Sale '{}' (ID: {}) chuyển sang trạng thái ACTIVE", sale.getName(), sale.getId());
            } else {
                sale.setStatus(EFlashSaleStatus.EXPIRED);
                log.info("Flash Sale '{}' (ID: {}) chuyển sang trạng thái EXPIRED", sale.getName(), sale.getId());
            }
        }

        // 2. Kết thúc các chiến dịch ACTIVE đã hết giờ
        List<FlashSale> toExpire = flashSaleRepository.findActiveSalesReadyToExpire(now);
        for (FlashSale sale : toExpire) {
            sale.setStatus(EFlashSaleStatus.EXPIRED);
            log.info("Flash Sale '{}' (ID: {}) chuyển sang trạng thái EXPIRED", sale.getName(), sale.getId());
        }
    }
}
