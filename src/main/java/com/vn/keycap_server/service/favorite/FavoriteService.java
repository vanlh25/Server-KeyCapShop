package com.vn.keycap_server.service.favorite;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.dto.request.cart.CartItemRequest;
import com.vn.keycap_server.dto.response.cart.CartCountResponse;
import com.vn.keycap_server.dto.response.favorite.ToggleFavoriteResponse;
import com.vn.keycap_server.dto.response.product.ProductCardResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.mapper.ProductMapper;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductVariant;
import com.vn.keycap_server.modal.User;
import com.vn.keycap_server.modal.Wishlist;
import com.vn.keycap_server.repository.ProductRepository;
import com.vn.keycap_server.repository.ProductVariantRepository;
import com.vn.keycap_server.repository.UserRepository;
import com.vn.keycap_server.repository.WishlistRepository;
import com.vn.keycap_server.service.cart.ICartService;
import com.vn.keycap_server.utils.EProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductMapper productMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ICartService cartService;

    @Override
    @Transactional
    public ToggleFavoriteResponse toggleFavorite(Long productId, Long userId) {

        // Check Existing Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check Existing User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check Existing Favorite
        var existingFavorite = wishlistRepository.findByProductIdAndUserId(productId, userId);

        if (existingFavorite.isPresent()) {
            wishlistRepository.deleteByProductIdAndUserId(productId, userId);
            return ToggleFavoriteResponse.builder()
                    .isFavorite(false)
                    .build();

        } else {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistRepository.save(wishlist);

            return ToggleFavoriteResponse.builder()
                    .isFavorite(true)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getUserFavorites(Long userId, int page, int limit) {
        int safePage = Math.max(1, page);
        int safeLimit = limit <= 0 ? 12 : limit;
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

        Page<Product> productPage = wishlistRepository.findWishlistProductsByUserId(userId, pageable);

        List<ProductCardResponse> content = productPage.getContent().stream()
                .map(product -> {
                    ProductCardResponse card = productMapper.productToProductCardResponse(product);
                    card.setFavorite(true);
                    return card;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional
    public void removeFavorite(Long productId, Long userId) {
        wishlistRepository.deleteByProductIdAndUserId(productId, userId);
    }

    @Override
    @Transactional
    public CartCountResponse moveToCart(Long productId, Long userId, Long variantId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        if (product.getStatus() == EProductStatus.UNAVAILABLE) {
            throw new BadRequestException("Sản phẩm hiện không còn được bán");
        }

        int safeQuantity = Math.max(1, quantity);

        ProductVariant targetVariant;
        if (variantId != null) {
            targetVariant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể không tồn tại"));
            if (!targetVariant.getProduct().getId().equals(productId)) {
                throw new BadRequestException("Biến thể không thuộc sản phẩm này");
            }
        } else {
            List<ProductVariant> variants = product.getVariants();
            if (variants == null || variants.isEmpty()) {
                throw new BadRequestException("Sản phẩm chưa có biến thể để thêm vào giỏ hàng");
            }
            targetVariant = variants.stream()
                    .filter(v -> v.getStockQuantity() != null && v.getStockQuantity() >= safeQuantity)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Sản phẩm này hiện đang hết hàng"));
        }

        // Thêm vào giỏ hàng
        CartCountResponse cartCount = cartService.addToCart(new CartItemRequest(targetVariant.getId(), safeQuantity));

        // Xóa khỏi danh sách yêu thích
        wishlistRepository.deleteByProductIdAndUserId(productId, userId);

        return cartCount;
    }

}
