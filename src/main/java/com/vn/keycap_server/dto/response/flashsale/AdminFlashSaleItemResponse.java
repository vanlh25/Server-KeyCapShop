package com.vn.keycap_server.dto.response.flashsale;

import java.time.LocalDateTime;

import com.vn.keycap_server.utils.EFlashSaleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFlashSaleItemResponse {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EFlashSaleStatus status;
    private int totalItems;
    private int totalSlots;
    private int soldSlots;
}
