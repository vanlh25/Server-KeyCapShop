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
import com.vn.keycap_server.dto.request.category.CreateCategoryRequest;
import com.vn.keycap_server.dto.request.category.UpdateCategoryRequest;
import com.vn.keycap_server.dto.response.CategoryResponse;
import com.vn.keycap_server.service.category.ICategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý API danh mục.
 * - GET list: dùng chung cho admin form (dropdown sản phẩm).
 * - POST/PATCH/DELETE: CRUD danh mục, bảo vệ bởi SecurityConfig (/admin/**).
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách danh mục thành công")
                .data(categories)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy thông tin danh mục thành công")
                .data(categoryService.getCategoryById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Tạo danh mục thành công")
                .data(categoryService.createCategory(request))
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật danh mục thành công")
                .data(categoryService.updateCategory(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Xóa danh mục thành công")
                .data(null)
                .build());
    }
}

