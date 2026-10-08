package com.vn.keycap_server.service.favorite;

import org.springframework.data.domain.Page;
import com.vn.keycap_server.dto.response.cart.CartCountResponse;
import com.vn.keycap_server.dto.response.favorite.ToggleFavoriteResponse;
import com.vn.keycap_server.dto.response.product.ProductCardResponse;

public interface IFavoriteService {

    ToggleFavoriteResponse toggleFavorite(Long productId, Long userId);

    Page<ProductCardResponse> getUserFavorites(Long userId, int page, int limit);

    void removeFavorite(Long productId, Long userId);

    CartCountResponse moveToCart(Long productId, Long userId, Long variantId, int quantity);
}

