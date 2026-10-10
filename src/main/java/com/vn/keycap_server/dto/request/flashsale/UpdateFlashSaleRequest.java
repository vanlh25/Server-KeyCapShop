package com.vn.keycap_server.dto.request.flashsale;

import java.time.LocalDateTime;

import com.vn.keycap_server.utils.EFlashSaleStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlashSaleRequest {

    @NotBlank(message = "Tên chiến dịch Flash Sale không được để trống")
    private String name;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    private EFlashSaleStatus status;
}
