package com.vn.keycap_server.dto.response.flashsale;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productThumbnail;
    private Long variantId;
    private String sku;
    private String variantAttributesSummary;
    private BigDecimal originalPrice;
    private BigDecimal flashSalePrice;
    private Integer discountPercent;
    private Integer totalSlots;
    private Integer soldSlots;
    private Integer userLimit;
}
