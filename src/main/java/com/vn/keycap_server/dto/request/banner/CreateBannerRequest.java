package com.vn.keycap_server.dto.request.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request tạo mới banner từ Admin.
 */
@Data
public class CreateBannerRequest {

    @NotBlank(message = "Tiêu đề banner không được để trống")
    @Size(max = 200, message = "Tiêu đề banner không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "URL hình ảnh không được để trống")
    private String imageUrl;

    private String linkUrl;

    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private int displayOrder = 0;

    private boolean active = true;
}
