package com.vn.keycap_server.service.flashsale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.vn.keycap_server.dto.response.flashsale.CustomerActiveFlashSaleResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerFlashSaleProductResponse;
import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.modal.FlashSaleItem;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.repository.FlashSaleItemRepository;
import com.vn.keycap_server.repository.FlashSaleRepository;
import com.vn.keycap_server.utils.EFlashSaleStatus;

@ExtendWith(MockitoExtension.class)
class CustomerFlashSaleServiceTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

    @InjectMocks
    private CustomerFlashSaleService customerFlashSaleService;

    private Product product;
    private ProductVariant variant;
    private FlashSale sale;
    private FlashSaleItem saleItem;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("MonsGeek M1 V3")
                .slug("monsgeek-m1-v3")
                .images(new ArrayList<>())
                .build();
        product.setId(1L);

        variant = ProductVariant.builder()
                .product(product)
                .sku("MG-M1V3-BLK")
                .price(BigDecimal.valueOf(2500000))
                .stockQuantity(100)
                .attributes(new ArrayList<>())
                .build();
        variant.setId(10L);

        sale = FlashSale.builder()
                .name("Flash Sale Trưa 12H")
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(EFlashSaleStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        sale.setId(100L);

        saleItem = FlashSaleItem.builder()
                .flashSale(sale)
                .product(product)
                .variant(variant)
                .originalPrice(BigDecimal.valueOf(2500000))
                .flashSalePrice(BigDecimal.valueOf(1990000))
                .totalSlots(20)
                .soldSlots(15)
                .userLimit(1)
                .build();
        saleItem.setId(500L);

        sale.getItems().add(saleItem);
    }

    @Test
    void getActiveFlashSale_Success() {
        when(flashSaleRepository.findCurrentActiveSales(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(sale));
        when(flashSaleRepository.findUpcomingSlots(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        CustomerActiveFlashSaleResponse response = customerFlashSaleService.getActiveFlashSale();

        assertNotNull(response);
        assertNotNull(response.getCurrentSlot());
        assertEquals("Flash Sale Trưa 12H", response.getCurrentSlot().getName());
        assertEquals(1, response.getCurrentSlot().getItems().size());

        CustomerFlashSaleProductResponse itemRes = response.getCurrentSlot().getItems().get(0);
        assertEquals(BigDecimal.valueOf(1990000), itemRes.getFlashSalePrice());
        assertEquals(75, itemRes.getPercentSold());
        assertTrue(itemRes.getIsHot());
        assertFalse(itemRes.getIsSoldOut());
    }

    @Test
    void getVariantFlashSale_Found() {
        when(flashSaleItemRepository.findActiveItemByVariantId(eq(10L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(saleItem));

        CustomerFlashSaleProductResponse response = customerFlashSaleService.getVariantFlashSale(10L);

        assertNotNull(response);
        assertEquals(10L, response.getVariantId());
        assertEquals("MG-M1V3-BLK", response.getSku());
        assertEquals(BigDecimal.valueOf(1990000), response.getFlashSalePrice());
    }

    @Test
    void getVariantFlashSale_NotFound() {
        when(flashSaleItemRepository.findActiveItemByVariantId(eq(999L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        CustomerFlashSaleProductResponse response = customerFlashSaleService.getVariantFlashSale(999L);

        assertNull(response);
    }
}
