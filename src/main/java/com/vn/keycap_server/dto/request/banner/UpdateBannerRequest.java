package com.vn.keycap_server.dto.request.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request cập nhật banner. Mọi field đều nullable.
 */
@Data
public class UpdateBannerRequest {

    @Size(max = 200, message = "Tiêu đề banner không được vượt quá 200 ký tự")
    private String title;

    private String imageUrl;

    private String linkUrl;

    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;

    private Boolean active;
}
