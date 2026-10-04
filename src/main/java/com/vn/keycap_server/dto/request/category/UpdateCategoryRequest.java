package com.vn.keycap_server.dto.request.category;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request cập nhật danh mục. Mọi field đều nullable – chỉ field nào FE gửi mới được cập nhật.
 */
@Data
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
}
