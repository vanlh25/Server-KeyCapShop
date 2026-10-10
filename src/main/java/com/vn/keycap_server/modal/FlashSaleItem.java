package com.vn.keycap_server.modal;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flash_sale_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flash_sale_variant", columnNames = { "flash_sale_id", "variant_id" })
})
@ToString(exclude = { "flashSale", "product", "variant" })
public class FlashSaleItem extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "flash_sale_price", nullable = false)
    private BigDecimal flashSalePrice;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Builder.Default
    @Column(name = "sold_slots", nullable = false)
    private Integer soldSlots = 0;

    @Builder.Default
    @Column(name = "user_limit", nullable = false)
    private Integer userLimit = 1;
}
