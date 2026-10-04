package com.vn.keycap_server.service.category;

import java.util.List;

import com.vn.keycap_server.dto.request.category.CreateCategoryRequest;
import com.vn.keycap_server.dto.request.category.UpdateCategoryRequest;
import com.vn.keycap_server.dto.response.CategoryResponse;

/**
 * Contract nghiệp vụ cho danh mục sản phẩm.
 * Controller chỉ phụ thuộc vào interface này để giữ đúng phân tầng Controller -> Service.
 */
public interface ICategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
