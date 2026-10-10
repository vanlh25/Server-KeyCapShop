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
public class CustomerFlashSaleProductResponse {
    private Long productId;
    private String productName;
    private String slug;
    private String thumbnailUrl;
    private Long variantId;
    private String sku;
    private String variantAttributes;
    private BigDecimal originalPrice;
    private BigDecimal flashSalePrice;
    private Integer discountPercent;
    private Integer totalSlots;
    private Integer soldSlots;
    private Integer percentSold;
    private Integer userLimit;
    private Boolean isSoldOut;
    private Boolean isHot;
}
