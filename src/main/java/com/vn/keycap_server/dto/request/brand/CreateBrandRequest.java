package com.vn.keycap_server.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request tạo mới thương hiệu từ Admin.
 */
@Data
public class CreateBrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    // URL logo thương hiệu (upload trước qua /admin/media, sau đó gửi URL lên)
    private String imageUrl;
}
