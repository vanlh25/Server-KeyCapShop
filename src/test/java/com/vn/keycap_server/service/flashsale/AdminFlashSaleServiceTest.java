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

import com.vn.keycap_server.dto.request.flashsale.CreateFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.FlashSaleItemRequest;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleDetailResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.repository.FlashSaleItemRepository;
import com.vn.keycap_server.repository.FlashSaleRepository;
import com.vn.keycap_server.repository.ProductRepository;
import com.vn.keycap_server.repository.ProductVariantRepository;
import com.vn.keycap_server.utils.EFlashSaleStatus;

@ExtendWith(MockitoExtension.class)
class AdminFlashSaleServiceTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;
    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private AdminFlashSaleService adminFlashSaleService;

    private Product testProduct;
    private ProductVariant testVariant;



    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("Bàn phím cơ Custom 65%")
                .images(new ArrayList<>())
                .build();
        testProduct.setId(10L);

        testVariant = ProductVariant.builder()
                .product(testProduct)
                .sku("KEY-65-RED")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(50)
                .attributes(new ArrayList<>())
                .build();
        testVariant.setId(20L);
    }

    @Test
    void createFlashSale_Success() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(5);

        FlashSaleItemRequest itemReq = FlashSaleItemRequest.builder()
                .productId(10L)
                .variantId(20L)
                .flashSalePrice(BigDecimal.valueOf(699000))
                .totalSlots(20)
                .userLimit(1)
                .build();

        CreateFlashSaleRequest request = CreateFlashSaleRequest.builder()
                .name("Flash Sale Giữa Tháng")
                .startTime(start)
                .endTime(end)
                .items(List.of(itemReq))
                .build();

        FlashSale mockSaved = FlashSale.builder()
                .name(request.getName())
                .startTime(start)
                .endTime(end)
                .status(EFlashSaleStatus.UPCOMING)
                .items(new ArrayList<>())
                .build();
        mockSaved.setId(1L);

        when(flashSaleRepository.save(any(FlashSale.class))).thenReturn(mockSaved);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.findById(20L)).thenReturn(Optional.of(testVariant));
        when(flashSaleItemRepository.existsOverlappingSaleForVariant(eq(20L), eq(start), eq(end), eq(1L)))
                .thenReturn(false);

        AdminFlashSaleDetailResponse response = adminFlashSaleService.createFlashSale(request);

        assertNotNull(response);
        assertEquals("Flash Sale Giữa Tháng", response.getName());
        assertEquals(EFlashSaleStatus.UPCOMING, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(BigDecimal.valueOf(699000), response.getItems().get(0).getFlashSalePrice());
    }

    @Test
    void createFlashSale_InvalidTime_ThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = LocalDateTime.now().plusHours(1); // end before start

        CreateFlashSaleRequest request = CreateFlashSaleRequest.builder()
                .name("Lỗi Giờ")
                .startTime(start)
                .endTime(end)
                .items(List.of())
                .build();

        assertThrows(BadRequestException.class, () -> adminFlashSaleService.createFlashSale(request));
    }

    @Test
    void createFlashSale_PriceHigherThanOriginal_ThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(5);

        FlashSaleItemRequest itemReq = FlashSaleItemRequest.builder()
                .productId(10L)
                .variantId(20L)
                .flashSalePrice(BigDecimal.valueOf(1200000)) // higher than variant price 1,000,000
                .totalSlots(10)
                .userLimit(1)
                .build();

        CreateFlashSaleRequest request = CreateFlashSaleRequest.builder()
                .name("Giá Sai")
                .startTime(start)
                .endTime(end)
                .items(List.of(itemReq))
                .build();

        FlashSale mockSaved = FlashSale.builder()
                .name(request.getName())
                .startTime(start)
                .endTime(end)
                .status(EFlashSaleStatus.UPCOMING)
                .items(new ArrayList<>())
                .build();
        mockSaved.setId(1L);

        when(flashSaleRepository.save(any(FlashSale.class))).thenReturn(mockSaved);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.findById(20L)).thenReturn(Optional.of(testVariant));

        assertThrows(BadRequestException.class, () -> adminFlashSaleService.createFlashSale(request));
    }

    @Test
    void cancelFlashSale_Success() {
        FlashSale sale = FlashSale.builder()
                .name("Sale Test")
                .status(EFlashSaleStatus.ACTIVE)
                .build();
        sale.setId(5L);

        when(flashSaleRepository.findById(5L)).thenReturn(Optional.of(sale));

        adminFlashSaleService.cancelFlashSale(5L);

        assertEquals(EFlashSaleStatus.CANCELLED, sale.getStatus());
        verify(flashSaleRepository).save(sale);
    }
}
