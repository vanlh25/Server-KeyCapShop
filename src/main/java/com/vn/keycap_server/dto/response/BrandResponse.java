package com.vn.keycap_server.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin thương hiệu cho FE admin.
 * id, name, slug dùng cho dropdown sản phẩm.
 * imageUrl, description, createdAt dùng thêm cho trang quản lý thương hiệu (Admin CRUD).
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BrandResponse {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private String description;
    private LocalDate createdAt;
}

