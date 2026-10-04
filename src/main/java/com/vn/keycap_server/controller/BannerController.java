package com.vn.keycap_server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.response.banner.BannerResponse;
import com.vn.keycap_server.service.banner.IBannerService;

import lombok.RequiredArgsConstructor;

/**
 * Controller public để lấy banner active cho slider trang chủ.
 * Không cần xác thực vì trang chủ là public.
 */
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final IBannerService bannerService;

    /**
     * Lấy danh sách banner đang active để render slider trang chủ.
     * Sắp xếp theo displayOrder tăng dần.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getActiveBanners() {
        List<BannerResponse> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách banner thành công")
                .data(banners)
                .build());
    }
}
