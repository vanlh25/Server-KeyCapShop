package com.vn.keycap_server.dto.response.order;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private List<AttributeResponse> attributes;
    private boolean isReviewed;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttributeResponse {
        private String name;
        private String value;
    }
}
