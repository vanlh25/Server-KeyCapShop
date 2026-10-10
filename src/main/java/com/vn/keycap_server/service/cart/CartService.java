package com.vn.keycap_server.service.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.dto.request.cart.CartItemRequest;
import com.vn.keycap_server.dto.response.cart.CartCountResponse;
import com.vn.keycap_server.dto.response.cart.CartDetailResponse;
import com.vn.keycap_server.dto.response.cart.CartItemDetailResponse;
import com.vn.keycap_server.dto.response.cart.CartProductResponse;
import com.vn.keycap_server.dto.response.cart.CartSummaryResponse;
import com.vn.keycap_server.dto.response.cart.CartVariantResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.exception.UnauthorizedException;
import com.vn.keycap_server.modal.CartItem;
import com.vn.keycap_server.modal.FlashSaleItem;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.modal.ProductImage;
import com.vn.keycap_server.modal.ProductVariantAttribute;
import com.vn.keycap_server.repository.CartItemRepository;
import com.vn.keycap_server.repository.FlashSaleItemRepository;
import com.vn.keycap_server.repository.ProductImageRepository;
import com.vn.keycap_server.repository.ProductVariantRepository;
import com.vn.keycap_server.utils.EProductStatus;

import lombok.RequiredArgsConstructor;

/**
 * CartService xử lý nghiệp vụ giỏ hàng.
 */
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private static final int MAX_QUANTITY_PER_CART_ITEM = 100;

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    /**
     * Lấy chi tiết giỏ hàng của user, bao gồm product, variant, attributes và
     * summary.
     *
     * @param userId ID của user đang đăng nhập
     * @return chi tiết giỏ hàng; giỏ rỗng trả items rỗng và summary bằng 0
     */
    @Override
    @Transactional(readOnly = true)
    public CartDetailResponse getCart(Long userId) {
        // B1: Lấy cart item cùng product, variant và attributes trong một query.
        List<CartItem> cartItems = cartItemRepository.findCartDetailsByUserId(userId);

        // B2: Trả response rỗng đúng contract FE nếu user chưa có item.
        if (cartItems.isEmpty()) {
            return CartDetailResponse.builder()
                    .items(Collections.emptyList())
                    .summary(CartSummaryResponse.builder()
                            .total(BigDecimal.ZERO)
                            .cartCount(0)
                            .build())
                    .build();
        }

        // B3: Batch load ảnh của các product để tránh N+1 query.
        List<Long> productIds = cartItems.stream()
                .map(item -> item.getVariant().getProduct().getId())
                .distinct()
                .toList();
        Map<Long, String> displayImageByProductId = productImageRepository.findDisplayImagesByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getId(),
                        ProductImage::getUrl,
                        (first, ignored) -> first,
                        LinkedHashMap::new));

        // B4: Map từng cart item và tính summary trong cùng một lần duyệt.
        int cartCount = 0;
        BigDecimal total = BigDecimal.ZERO;
        List<CartItemDetailResponse> items = new java.util.ArrayList<>(cartItems.size());

        LocalDateTime now = LocalDateTime.now();
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            int quantity = cartItem.getQuantity();

            List<FlashSaleItem> activeItems = flashSaleItemRepository.findActiveItemsByVariantId(variant.getId(), now);
            FlashSaleItem activeItem = activeItems.stream().findFirst().orElse(null);

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal originalPrice = variant.getOriginalPrice() != null ? variant.getOriginalPrice() : variant.getPrice();
            Integer discountPercent = variant.getPercentDiscount();

            if (activeItem != null && activeItem.getSoldSlots() < activeItem.getTotalSlots()) {
                unitPrice = activeItem.getFlashSalePrice();
                originalPrice = activeItem.getOriginalPrice() != null ? activeItem.getOriginalPrice() : variant.getPrice();
                if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = originalPrice.subtract(unitPrice);
                    discountPercent = diff.multiply(BigDecimal.valueOf(100))
                            .divide(originalPrice, 0, RoundingMode.HALF_UP)
                            .intValue();
                }
            }

            cartCount += quantity;
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));

            // Nếu dữ liệu cũ có attribute trùng tên, ưu tiên giá trị đầu tiên.
            Map<String, String> attributes = variant.getAttributes() == null
                    ? Collections.emptyMap()
                    : variant.getAttributes().stream().collect(Collectors.toMap(
                            ProductVariantAttribute::getName,
                            ProductVariantAttribute::getValue,
                            (first, ignored) -> first,
                            LinkedHashMap::new));

            items.add(CartItemDetailResponse.builder()
                    .id(cartItem.getId())
                    .product(CartProductResponse.builder()
                            .id(variant.getProduct().getId())
                            .name(variant.getProduct().getName())
                            .slug(variant.getProduct().getSlug())
                            .imageUrl(displayImageByProductId.get(variant.getProduct().getId()))
                            .build())
                    .variant(CartVariantResponse.builder()
                            .id(variant.getId())
                            .attributes(attributes)
                            .price(unitPrice)
                            .originalPrice(originalPrice)
                            .percentDiscount(discountPercent)
                            .quantity(quantity)
                            .stockQuantity(variant.getStockQuantity())
                            .build())
                    .build());
        }

        // B5: Đóng gói items và summary theo CartDetailModel của FE.
        return CartDetailResponse.builder()
                .items(items)
                .summary(CartSummaryResponse.builder()
                        .total(total)
                        .cartCount(cartCount)
                        .build())
                .build();
    }

    /**
     * Lấy thông tin tóm tắt về số lượng sản phẩm hiện có trong giỏ hàng của người
     * dùng.
     *
     * @return CartCountResponse đối tượng chứa tổng số lượng vật phẩm trong giỏ
     */
    @Override
    @Transactional(readOnly = true)
    public CartCountResponse getCartSummary() {
        Long userId = getCurrentUserId();
        int count = cartItemRepository.sumQuantityByUserId(userId);
        return CartCountResponse.builder().cartCount(count).build();
    }

    /**
     * Thêm một sản phẩm vào giỏ hàng hoặc tăng số lượng nếu sản phẩm đã tồn tại.
     *
     * @param request đối tượng chứa thông tin ID sản phẩm và số lượng cần thêm
     * @return CartCountResponse đối tượng chứa tổng số lượng mới của giỏ hàng
     */
    @Override
    @Transactional
    public CartCountResponse addToCart(CartItemRequest request) {
        Long userId = getCurrentUserId();

        // Kiểm tra biến thể sản phẩm có tồn tại không
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm"));

        // Kiểm tra biến thể sản phẩm có thể thêm vào giỏ hàng không (trạng thái, tồn
        // kho)
        validateVariantCanBeAddedToCart(variant, request.getQuantity());

        //// Thực hiện upsert: nếu biến thể sản phẩm đã có trong giỏ thì cập nhật số
        //// lượng, nếu chưa có thì thêm mới
        int changedRows = cartItemRepository.upsertCartItemIfWithinStock(
                userId,
                request.getVariantId(),
                request.getQuantity(),
                MAX_QUANTITY_PER_CART_ITEM);
        // Nếu không có hàng nào bị cập nhật, có thể do vượt quá số lượng tồn kho hoặc
        // giới hạn tối đa, cần kiểm tra lại
        if (changedRows == 0) {
            cartItemRepository.findByUserIdAndVariantId(userId, request.getVariantId())
                    .ifPresent(item -> validateCartItemQuantity(
                            item.getQuantity() + request.getQuantity(),
                            variant.getStockQuantity()));
            throw new BadRequestException("Số lượng biến thể sản phẩm trong kho không đủ");
        }

        int newCount = cartItemRepository.sumQuantityByUserId(userId);
        return CartCountResponse.builder().newCartCount(newCount).build();
    }

    /**
     * Cập nhật số lượng hàng loạt cho các sản phẩm hiện có trong giỏ hàng của người
     * dùng.
     * * @param requests danh sách các yêu cầu thay đổi số lượng kèm ID sản phẩm
     * tương ứng
     * 
     * @return CartCountResponse đối tượng chứa tổng số lượng mới của giỏ hàng sau
     *         khi cập nhật
     */
    @Override
    @Transactional
    public CartCountResponse updateCartItems(List<CartItemRequest> requests) {
        Long userId = getCurrentUserId();

        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Danh sách cập nhật trống");
        }

        // Lấy toàn bộ giỏ hàng hiện tại của user để xử lý batch in memory
        List<CartItem> currentItems = cartItemRepository.findByUserId(userId);
        Map<Long, CartItem> itemByVariantId = currentItems.stream()
                .collect(Collectors.toMap(item -> item.getVariant().getId(), item -> item));

        for (CartItemRequest request : requests) {
            CartItem item = itemByVariantId.get(request.getVariantId());
            if (item != null) {
                validateVariantCanBeAddedToCart(item.getVariant(), request.getQuantity());
                item.setQuantity(request.getQuantity());
            }
        }

        int newCount = cartItemRepository.sumQuantityByUserId(userId);
        BigDecimal totalPrice = cartItemRepository.sumTotalPriceByUserId(userId);
        return CartCountResponse.builder()
                .newCartCount(newCount)
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * Xóa bỏ hoàn toàn một sản phẩm ra khỏi giỏ hàng của người dùng.
     *
     * @param productId ID của sản phẩm cần xóa
     * @return CartCountResponse đối tượng chứa tổng số lượng còn lại của giỏ hàng
     */
    @Override
    @Transactional
    public CartCountResponse deleteCartItem(Long variantId) {
        Long userId = getCurrentUserId();
        cartItemRepository.deleteByUserIdAndVariantId(userId, variantId);
        int newCount = cartItemRepository.sumQuantityByUserId(userId);
        BigDecimal totalPrice = cartItemRepository.sumTotalPriceByUserId(userId);
        return CartCountResponse.builder()
                .newCartCount(newCount)
                .totalPrice(totalPrice)
                .build();
    }

    // =================================================
    // Các phương thức hỗ trợ
    // =================================================

    /**
     * Lấy ID user từ Security Context. Bắt buộc phải đăng nhập.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            if (jwtAuthenticationToken.isAuthenticated()) {
                Object userIdClaim = jwtAuthenticationToken.getTokenAttributes().get("userId");
                if (userIdClaim instanceof Number) {
                    return ((Number) userIdClaim).longValue();
                }
                if (userIdClaim instanceof String userIdText) {
                    try {
                        return Long.parseLong(userIdText);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        throw new UnauthorizedException("Vui lòng đăng nhập để sử dụng tính năng này");
    }

    // kiểm tra variant
    private void validateVariantCanBeAddedToCart(ProductVariant variant, int requestedQuantity) {
        if (variant.getProduct().getStatus() == EProductStatus.UNAVAILABLE) {
            throw new BadRequestException("Biến thể sản phẩm hiện không còn được bán");
        }
        Integer stockQuantity = variant.getStockQuantity();
        if (stockQuantity == null || stockQuantity <= 0) {
            throw new BadRequestException("Biến thể sản phẩm đã hết hàng");
        }
        validateCartItemQuantity(requestedQuantity, stockQuantity);
    }

    private void validateCartItemQuantity(int quantity, int stock) {
        if (quantity > MAX_QUANTITY_PER_CART_ITEM) {
            throw new BadRequestException(
                    "Số lượng mỗi sản phẩm trong giỏ hàng không được vượt quá " + MAX_QUANTITY_PER_CART_ITEM);
        }
        if (quantity > stock) {
            throw new BadRequestException("So luong san pham trong kho khong du");
        }
    }
}
