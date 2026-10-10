package com.vn.keycap_server.dto.request.flashsale;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItemRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "variantId không được để trống")
    private Long variantId;

    @NotNull(message = "flashSalePrice không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "flashSalePrice phải lớn hơn 0")
    private BigDecimal flashSalePrice;

    @NotNull(message = "totalSlots không được để trống")
    @Min(value = 1, message = "totalSlots phải lớn hơn hoặc bằng 1")
    private Integer totalSlots;

    @Builder.Default
    @NotNull(message = "userLimit không được để trống")
    @Min(value = 1, message = "userLimit phải lớn hơn hoặc bằng 1")
    private Integer userLimit = 1;
}
