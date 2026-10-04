package com.vn.keycap_server.dto.request.brand;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request cập nhật thương hiệu. Mọi field đều nullable.
 */
@Data
public class UpdateBrandRequest {

    @Size(max = 100, message = "Tên thương hiệu không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private String imageUrl;
}
