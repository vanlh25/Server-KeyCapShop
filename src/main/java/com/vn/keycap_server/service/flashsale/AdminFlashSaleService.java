package com.vn.keycap_server.service.flashsale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vn.keycap_server.dto.request.flashsale.AdminListFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.CreateFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.FlashSaleItemRequest;
import com.vn.keycap_server.dto.request.flashsale.UpdateFlashSaleRequest;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleDetailResponse;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleItemResponse;
import com.vn.keycap_server.dto.response.flashsale.FlashSaleItemResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.modal.FlashSale;
import com.vn.keycap_server.modal.FlashSaleItem;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductImage;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.repository.FlashSaleItemRepository;
import com.vn.keycap_server.repository.FlashSaleRepository;
import com.vn.keycap_server.repository.ProductRepository;
import com.vn.keycap_server.repository.ProductVariantRepository;
import com.vn.keycap_server.utils.EFlashSaleStatus;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminFlashSaleService implements IAdminFlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminFlashSaleItemResponse> getFlashSales(AdminListFlashSaleRequest request) {
        int pageIndex = Math.max(0, request.getPage() - 1);
        Pageable pageable = PageRequest.of(pageIndex, request.getLimit(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<FlashSale> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearch())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getSearch().trim().toLowerCase() + "%"));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<FlashSale> salesPage = flashSaleRepository.findAll(spec, pageable);
        return salesPage.map(this::mapToAdminFlashSaleItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFlashSaleDetailResponse getFlashSaleById(Long id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch Flash Sale với ID: " + id));
        return mapToDetailResponse(flashSale);
    }

    @Override
    @Transactional
    public AdminFlashSaleDetailResponse createFlashSale(CreateFlashSaleRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        LocalDateTime now = LocalDateTime.now();
        if (request.getEndTime().isBefore(now)) {
            throw new BadRequestException("Thời gian kết thúc không thể ở trong quá khứ");
        }

        EFlashSaleStatus initialStatus = EFlashSaleStatus.UPCOMING;
        if (!request.getStartTime().isAfter(now) && request.getEndTime().isAfter(now)) {
            initialStatus = EFlashSaleStatus.ACTIVE;
        }

        FlashSale flashSale = FlashSale.builder()
                .name(request.getName().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(initialStatus)
                .items(new ArrayList<>())
                .build();

        FlashSale savedSale = flashSaleRepository.save(flashSale);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<FlashSaleItem> saleItems = buildAndValidateItems(savedSale, request.getItems(), request.getStartTime(), request.getEndTime(), savedSale.getId());
            savedSale.getItems().addAll(saleItems);
            flashSaleRepository.save(savedSale);
        }

        return mapToDetailResponse(savedSale);
    }

    @Override
    @Transactional
    public AdminFlashSaleDetailResponse updateFlashSale(Long id, UpdateFlashSaleRequest request) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch Flash Sale với ID: " + id));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Nếu thay đổi khung giờ, kiểm tra lại conflict cho các item hiện có
        boolean timeChanged = !flashSale.getStartTime().isEqual(request.getStartTime()) || !flashSale.getEndTime().isEqual(request.getEndTime());
        if (timeChanged && flashSale.getItems() != null) {
            for (FlashSaleItem item : flashSale.getItems()) {
                boolean overlapping = flashSaleItemRepository.existsOverlappingSaleForVariant(
                        item.getVariant().getId(), request.getStartTime(), request.getEndTime(), flashSale.getId());
                if (overlapping) {
                    throw new BadRequestException("Biến thể SKU " + item.getVariant().getSku()
                            + " bị trùng với một chiến dịch Flash Sale khác trong khung giờ mới này");
                }
            }
        }

        flashSale.setName(request.getName().trim());
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());

        if (request.getStatus() != null) {
            flashSale.setStatus(request.getStatus());
        } else {
            // Tự động đồng bộ trạng thái theo thời gian nếu chưa CANCELLED
            if (flashSale.getStatus() != EFlashSaleStatus.CANCELLED) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(request.getStartTime())) {
                    flashSale.setStatus(EFlashSaleStatus.UPCOMING);
                } else if (!now.isBefore(request.getStartTime()) && !now.isAfter(request.getEndTime())) {
                    flashSale.setStatus(EFlashSaleStatus.ACTIVE);
                } else {
                    flashSale.setStatus(EFlashSaleStatus.EXPIRED);
                }
            }
        }

        FlashSale updated = flashSaleRepository.save(flashSale);
        return mapToDetailResponse(updated);
    }

    @Override
    @Transactional
    public void cancelFlashSale(Long id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch Flash Sale với ID: " + id));

        flashSale.setStatus(EFlashSaleStatus.CANCELLED);
        flashSaleRepository.save(flashSale);
    }

    @Override
    @Transactional
    public AdminFlashSaleDetailResponse addItemsToFlashSale(Long id, List<FlashSaleItemRequest> items) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch Flash Sale với ID: " + id));

        if (flashSale.getStatus() == EFlashSaleStatus.CANCELLED || flashSale.getStatus() == EFlashSaleStatus.EXPIRED) {
            throw new BadRequestException("Không thể thêm sản phẩm vào chiến dịch đã kết thúc hoặc đã hủy");
        }

        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Danh sách sản phẩm thêm vào không được rỗng");
        }

        List<FlashSaleItem> newItems = buildAndValidateItems(flashSale, items, flashSale.getStartTime(), flashSale.getEndTime(), flashSale.getId());
        flashSale.getItems().addAll(newItems);

        FlashSale updated = flashSaleRepository.save(flashSale);
        return mapToDetailResponse(updated);
    }

    @Override
    @Transactional
    public void removeItemFromFlashSale(Long flashSaleId, Long itemId) {
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch Flash Sale với ID: " + flashSaleId));

        FlashSaleItem itemToRemove = flashSale.getItems().stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm Flash Sale với ID: " + itemId));

        flashSale.getItems().remove(itemToRemove);
        flashSaleItemRepository.delete(itemToRemove);
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }

    private List<FlashSaleItem> buildAndValidateItems(FlashSale flashSale, List<FlashSaleItemRequest> items,
                                                     LocalDateTime start, LocalDateTime end, Long saleId) {
        List<FlashSaleItem> result = new ArrayList<>();
        Set<Long> existingVariantIds = flashSale.getItems() != null
                ? flashSale.getItems().stream().map(i -> i.getVariant().getId()).collect(Collectors.toSet())
                : new HashSet<>();

        Set<Long> currentBatchVariantIds = new HashSet<>();

        for (FlashSaleItemRequest req : items) {
            if (currentBatchVariantIds.contains(req.getVariantId()) || existingVariantIds.contains(req.getVariantId())) {
                throw new BadRequestException("Biến thể ID " + req.getVariantId() + " bị trùng lặp trong chiến dịch này");
            }
            currentBatchVariantIds.add(req.getVariantId());

            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + req.getProductId()));

            ProductVariant variant = productVariantRepository.findById(req.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể ID: " + req.getVariantId()));

            if (!Objects.equals(variant.getProduct().getId(), product.getId())) {
                throw new BadRequestException("Biến thể SKU " + variant.getSku() + " không thuộc về sản phẩm " + product.getName());
            }

            BigDecimal originalPrice = variant.getPrice();
            if (req.getFlashSalePrice().compareTo(BigDecimal.ZERO) <= 0 || req.getFlashSalePrice().compareTo(originalPrice) >= 0) {
                throw new BadRequestException("Giá Flash Sale cho SKU " + variant.getSku()
                        + " (" + req.getFlashSalePrice() + ") phải lớn hơn 0 và nhỏ hơn giá bán hiện tại (" + originalPrice + ")");
            }

            if (req.getTotalSlots() > variant.getStockQuantity()) {
                throw new BadRequestException("Số suất bán cho SKU " + variant.getSku()
                        + " (" + req.getTotalSlots() + ") không được vượt quá tồn kho thực tế (" + variant.getStockQuantity() + ")");
            }

            // Kiểm tra trùng lịch với chiến dịch khác
            boolean overlapping = flashSaleItemRepository.existsOverlappingSaleForVariant(variant.getId(), start, end, saleId);
            if (overlapping) {
                throw new BadRequestException("Biến thể SKU " + variant.getSku()
                        + " đã tham gia một chiến dịch Flash Sale khác trong cùng khung giờ này");
            }

            FlashSaleItem item = FlashSaleItem.builder()
                    .flashSale(flashSale)
                    .product(product)
                    .variant(variant)
                    .originalPrice(originalPrice)
                    .flashSalePrice(req.getFlashSalePrice())
                    .totalSlots(req.getTotalSlots())
                    .soldSlots(0)
                    .userLimit(req.getUserLimit() != null && req.getUserLimit() > 0 ? req.getUserLimit() : 1)
                    .build();

            result.add(item);
        }

        return result;
    }

    private AdminFlashSaleItemResponse mapToAdminFlashSaleItemResponse(FlashSale sale) {
        int totalItems = sale.getItems() != null ? sale.getItems().size() : 0;
        int totalSlots = sale.getItems() != null ? sale.getItems().stream().mapToInt(FlashSaleItem::getTotalSlots).sum() : 0;
        int soldSlots = sale.getItems() != null ? sale.getItems().stream().mapToInt(FlashSaleItem::getSoldSlots).sum() : 0;

        return AdminFlashSaleItemResponse.builder()
                .id(sale.getId())
                .name(sale.getName())
                .startTime(sale.getStartTime())
                .endTime(sale.getEndTime())
                .status(sale.getStatus())
                .totalItems(totalItems)
                .totalSlots(totalSlots)
                .soldSlots(soldSlots)
                .build();
    }

    private AdminFlashSaleDetailResponse mapToDetailResponse(FlashSale sale) {
        List<FlashSaleItemResponse> itemResponses = new ArrayList<>();
        if (sale.getItems() != null) {
            for (FlashSaleItem item : sale.getItems()) {
                Product product = item.getProduct();
                ProductVariant variant = item.getVariant();

                String thumbnail = product.getImages() != null && !product.getImages().isEmpty()
                        ? product.getImages().stream()
                                .filter(img -> Boolean.TRUE.equals(img.getPrimary()))
                                .findFirst()
                                .map(ProductImage::getUrl)
                                .orElse(product.getImages().get(0).getUrl())
                        : null;

                String attributesSummary = variant.getAttributes() != null && !variant.getAttributes().isEmpty()
                        ? variant.getAttributes().stream()
                                .map(attr -> attr.getName() + ": " + attr.getValue())
                                .collect(Collectors.joining(", "))
                        : "";

                int discountPercent = 0;
                if (item.getOriginalPrice() != null && item.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = item.getOriginalPrice().subtract(item.getFlashSalePrice());
                    discountPercent = diff.multiply(BigDecimal.valueOf(100))
                            .divide(item.getOriginalPrice(), 0, RoundingMode.HALF_UP)
                            .intValue();
                }

                itemResponses.add(FlashSaleItemResponse.builder()
                        .id(item.getId())
                        .productId(product.getId())
                        .productName(product.getName())
                        .productThumbnail(thumbnail)
                        .variantId(variant.getId())
                        .sku(variant.getSku())
                        .variantAttributesSummary(attributesSummary)
                        .originalPrice(item.getOriginalPrice())
                        .flashSalePrice(item.getFlashSalePrice())
                        .discountPercent(discountPercent)
                        .totalSlots(item.getTotalSlots())
                        .soldSlots(item.getSoldSlots())
                        .userLimit(item.getUserLimit())
                        .build());
            }
        }

        return AdminFlashSaleDetailResponse.builder()
                .id(sale.getId())
                .name(sale.getName())
                .startTime(sale.getStartTime())
                .endTime(sale.getEndTime())
                .status(sale.getStatus())
                .items(itemResponses)
                .build();
    }
}
