package com.vn.keycap_server.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.request.banner.CreateBannerRequest;
import com.vn.keycap_server.dto.request.banner.UpdateBannerRequest;
import com.vn.keycap_server.dto.response.banner.BannerResponse;
import com.vn.keycap_server.service.banner.IBannerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý banner quảng cáo trang chủ.
 * ADMIN và STAFF đều có thể quản lý banner.
 */
@Validated
@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AdminBannerController {

    private final IBannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllBanners() {
        List<BannerResponse> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách banner thành công")
                .data(banners)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy thông tin banner thành công")
                .data(bannerService.getBannerById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createBanner(@Valid @RequestBody CreateBannerRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Tạo banner thành công")
                .data(bannerService.createBanner(request))
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBannerRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật banner thành công")
                .data(bannerService.updateBanner(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Xóa banner thành công")
                .data(null)
                .build());
    }
}
