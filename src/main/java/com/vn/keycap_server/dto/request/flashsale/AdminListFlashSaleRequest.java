package com.vn.keycap_server.dto.request.flashsale;

import com.vn.keycap_server.utils.EFlashSaleStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListFlashSaleRequest {

    @Builder.Default
    @Min(value = 1, message = "page phải lớn hơn hoặc bằng 1")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "limit phải lớn hơn hoặc bằng 1")
    @Max(value = 100, message = "limit không được vượt quá 100")
    private int limit = 20;

    private String search;

    private EFlashSaleStatus status;
}
