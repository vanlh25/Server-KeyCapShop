package com.vn.keycap_server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.request.brand.CreateBrandRequest;
import com.vn.keycap_server.dto.request.brand.UpdateBrandRequest;
import com.vn.keycap_server.dto.response.BrandResponse;
import com.vn.keycap_server.service.brand.IBrandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý API thương hiệu.
 * - GET list: dùng chung cho admin form (dropdown sản phẩm).
 * - POST/PATCH/DELETE: CRUD thương hiệu, bảo vệ bởi SecurityConfig (/admin/**).
 */
@RestController
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class BrandController {

    private final IBrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse> getBrands() {
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách thương hiệu thành công")
                .data(brands)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy thông tin thương hiệu thành công")
                .data(brandService.getBrandById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Tạo thương hiệu thành công")
                .data(brandService.createBrand(request))
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật thương hiệu thành công")
                .data(brandService.updateBrand(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Xóa thương hiệu thành công")
                .data(null)
                .build());
    }
}

