package com.vn.keycap_server.service.flashsale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.dto.response.flashsale.CustomerActiveFlashSaleResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerFlashSaleProductResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerTimeSlotResponse;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.modal.FlashSaleItem;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductImage;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.repository.FlashSaleItemRepository;
import com.vn.keycap_server.repository.FlashSaleRepository;
import com.vn.keycap_server.utils.EFlashSaleStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerFlashSaleService implements ICustomerFlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerActiveFlashSaleResponse getActiveFlashSale() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Lấy tất cả chiến dịch đang ACTIVE hiện tại (tối đa 10)
        List<FlashSale> activeSales = flashSaleRepository.findCurrentActiveSales(now, PageRequest.of(0, 10));
        List<CustomerTimeSlotResponse> activeSlots = activeSales.stream()
                .map(sale -> mapToTimeSlot(sale, now))
                .toList();
        CustomerTimeSlotResponse currentSlot = !activeSlots.isEmpty() ? activeSlots.get(0) : null;

        // 2. Lấy tối đa 4 khung giờ sắp diễn ra tiếp theo
        List<FlashSale> upcomingSales = flashSaleRepository.findUpcomingSlots(now, PageRequest.of(0, 4));
        List<CustomerTimeSlotResponse> upcomingSlots = upcomingSales.stream()
                .map(sale -> mapToTimeSlot(sale, now))
                .toList();

        return CustomerActiveFlashSaleResponse.builder()
                .currentSlot(currentSlot)
                .activeSlots(activeSlots)
                .upcomingSlots(upcomingSlots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerTimeSlotResponse> getUpcomingSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> upcomingSales = flashSaleRepository.findUpcomingSlots(now, PageRequest.of(0, 10));
        return upcomingSales.stream()
                .map(sale -> mapToTimeSlot(sale, now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerTimeSlotResponse getFlashSaleById(Long id) {
        FlashSale sale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt Flash Sale với ID: " + id));
        return mapToTimeSlot(sale, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerFlashSaleProductResponse getVariantFlashSale(Long variantId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<FlashSaleItem> itemOpt = flashSaleItemRepository.findActiveItemByVariantId(variantId, now);

        return itemOpt.map(this::mapToCustomerProduct).orElse(null);
    }

    private CustomerTimeSlotResponse mapToTimeSlot(FlashSale sale, LocalDateTime now) {
        long remainingSeconds = 0;
        if (sale.getStatus() == EFlashSaleStatus.ACTIVE) {
            remainingSeconds = Math.max(0, Duration.between(now, sale.getEndTime()).getSeconds());
        } else if (sale.getStatus() == EFlashSaleStatus.UPCOMING) {
            remainingSeconds = Math.max(0, Duration.between(now, sale.getStartTime()).getSeconds());
        }

        List<CustomerFlashSaleProductResponse> items = new ArrayList<>();
        if (sale.getItems() != null) {
            items = sale.getItems().stream()
                    .map(this::mapToCustomerProduct)
                    .sorted(Comparator.comparing(CustomerFlashSaleProductResponse::getIsSoldOut)
                            .thenComparing(Comparator.comparing(CustomerFlashSaleProductResponse::getPercentSold).reversed()))
                    .collect(Collectors.toList());
        }

        return CustomerTimeSlotResponse.builder()
                .flashSaleId(sale.getId())
                .name(sale.getName())
                .startTime(sale.getStartTime())
                .endTime(sale.getEndTime())
                .status(sale.getStatus())
                .remainingSeconds(remainingSeconds)
                .items(items)
                .build();
    }

    private CustomerFlashSaleProductResponse mapToCustomerProduct(FlashSaleItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();

        String thumbnail = product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getPrimary()))
                        .findFirst()
                        .map(ProductImage::getUrl)
                        .orElse(product.getImages().get(0).getUrl())
                : null;

        String attrSummary = variant.getAttributes() != null && !variant.getAttributes().isEmpty()
                ? variant.getAttributes().stream()
                        .map(a -> a.getName() + ": " + a.getValue())
                        .collect(Collectors.joining(", "))
                : "";

        int totalSlots = item.getTotalSlots() != null ? item.getTotalSlots() : 0;
        int soldSlots = item.getSoldSlots() != null ? item.getSoldSlots() : 0;
        int percentSold = totalSlots > 0 ? (int) Math.min(100, Math.round(((double) soldSlots / totalSlots) * 100)) : 0;

        int discountPercent = 0;
        if (item.getOriginalPrice() != null && item.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = item.getOriginalPrice().subtract(item.getFlashSalePrice());
            discountPercent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(item.getOriginalPrice(), 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        boolean isSoldOut = soldSlots >= totalSlots;
        boolean isHot = percentSold >= 70 && !isSoldOut;

        return CustomerFlashSaleProductResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(thumbnail)
                .variantId(variant.getId())
                .sku(variant.getSku())
                .variantAttributes(attrSummary)
                .originalPrice(item.getOriginalPrice())
                .flashSalePrice(item.getFlashSalePrice())
                .discountPercent(discountPercent)
                .totalSlots(totalSlots)
                .soldSlots(soldSlots)
                .percentSold(percentSold)
                .userLimit(item.getUserLimit() != null ? item.getUserLimit() : 1)
                .isSoldOut(isSoldOut)
                .isHot(isHot)
                .build();
    }
}
