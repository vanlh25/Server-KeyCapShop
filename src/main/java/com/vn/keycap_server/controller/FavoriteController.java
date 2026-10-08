package com.vn.keycap_server.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.PaginationMeta;
import com.vn.keycap_server.dto.response.product.ProductCardResponse;
import com.vn.keycap_server.exception.UnauthorizedException;
import com.vn.keycap_server.service.favorite.IFavoriteService;
import lombok.RequiredArgsConstructor;
import com.vn.keycap_server.utils.JwtUtils;
import com.vn.keycap_server.utils.PaginationUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    // Lấy danh sách sản phẩm yêu thích của user
    @GetMapping
    public ResponseEntity<ApiResponse> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để sử dụng tính năng này");
        }
        Long userId = JwtUtils.getUserId(jwt);
        Page<ProductCardResponse> resultPage = favoriteService.getUserFavorites(userId, page, limit);
        PaginationMeta meta = PaginationUtils.buildPaginationMeta(resultPage, page);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách yêu thích thành công")
                .data(resultPage.getContent())
                .pagination(meta)
                .build());
    }

    // Toggle Favorite Product For User
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse> addFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để sử dụng tính năng này");
        }
        Long userId = JwtUtils.getUserId(jwt);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Toggle favorite successfully")
                .data(favoriteService.toggleFavorite(productId, userId))
                .build());
    }

    // Xóa sản phẩm khỏi danh sách yêu thích
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để sử dụng tính năng này");
        }
        Long userId = JwtUtils.getUserId(jwt);
        favoriteService.removeFavorite(productId, userId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đã xóa sản phẩm khỏi danh sách yêu thích")
                .build());
    }

    // Chuyển sản phẩm từ yêu thích sang giỏ hàng
    @PostMapping("/{productId}/move-to-cart")
    public ResponseEntity<ApiResponse> moveToCart(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để sử dụng tính năng này");
        }
        Long userId = JwtUtils.getUserId(jwt);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đã chuyển sản phẩm vào giỏ hàng")
                .data(favoriteService.moveToCart(productId, userId, variantId, quantity))
                .build());
    }

}
