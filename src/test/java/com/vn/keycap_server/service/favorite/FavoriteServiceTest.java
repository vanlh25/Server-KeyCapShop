package com.vn.keycap_server.service.favorite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.vn.keycap_server.dto.request.cart.CartItemRequest;
import com.vn.keycap_server.dto.response.cart.CartCountResponse;
import com.vn.keycap_server.dto.response.favorite.ToggleFavoriteResponse;
import com.vn.keycap_server.dto.response.product.ProductCardResponse;
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

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ICartService cartService;

    @InjectMocks
    private FavoriteService favoriteService;

    private User user;
    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        user.setId(1L);

        variant = ProductVariant.builder()
                .sku("SKU-001")
                .price(BigDecimal.valueOf(100000))
                .originalPrice(BigDecimal.valueOf(120000))
                .stockQuantity(10)
                .build();
        variant.setId(10L);

        product = Product.builder()
                .name("Keycap Artisan")
                .slug("keycap-artisan")
                .status(EProductStatus.AVAILABLE)
                .variants(List.of(variant))
                .build();
        product.setId(100L);
        variant.setProduct(product);
    }

    @Test
    void toggleFavorite_WhenAlreadyExists_ShouldRemoveAndReturnFalse() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByProductIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(Wishlist.builder().product(product).user(user).build()));

        ToggleFavoriteResponse response = favoriteService.toggleFavorite(100L, 1L);

        assertFalse(response.getIsFavorite());
        verify(wishlistRepository).deleteByProductIdAndUserId(100L, 1L);
    }

    @Test
    void toggleFavorite_WhenNotExists_ShouldAddAndReturnTrue() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByProductIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        ToggleFavoriteResponse response = favoriteService.toggleFavorite(100L, 1L);

        assertTrue(response.getIsFavorite());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void getUserFavorites_ShouldReturnPageOfProductCardResponse() {
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(wishlistRepository.findWishlistProductsByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(productPage);

        ProductCardResponse card = ProductCardResponse.builder()
                .id(100L)
                .name("Keycap Artisan")
                .slug("keycap-artisan")
                .favorite(false)
                .build();
        when(productMapper.productToProductCardResponse(product)).thenReturn(card);

        Page<ProductCardResponse> result = favoriteService.getUserFavorites(1L, 1, 10);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isFavorite());
    }

    @Test
    void moveToCart_ShouldAddToCartAndRemoveFromWishlist() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartService.addToCart(any(CartItemRequest.class)))
                .thenReturn(CartCountResponse.builder().cartCount(1).build());

        CartCountResponse result = favoriteService.moveToCart(100L, 1L, null, 1);

        assertNotNull(result);
        assertEquals(1, result.getCartCount());
        verify(wishlistRepository).deleteByProductIdAndUserId(100L, 1L);
    }
}
