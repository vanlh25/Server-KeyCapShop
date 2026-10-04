package com.vn.keycap_server.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin danh mục cho FE admin.
 * Các field id, name, slug dùng cho dropdown sản phẩm.
 * description và createdAt dùng thêm cho trang quản lý danh mục (Admin CRUD).
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private LocalDate createdAt;
}

