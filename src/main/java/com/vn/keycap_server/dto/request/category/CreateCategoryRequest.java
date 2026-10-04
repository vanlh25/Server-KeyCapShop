package com.vn.keycap_server.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request tạo mới danh mục sản phẩm từ Admin.
 */
@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
}
