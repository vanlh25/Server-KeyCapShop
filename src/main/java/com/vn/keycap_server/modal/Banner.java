package com.vn.keycap_server.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity đại diện cho bảng "banners" – banner quảng cáo trang chủ.
 * Hỗ trợ hiển thị slider với thứ tự ưu tiên, bật/tắt từng banner.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "banners")
public class Banner extends AbstractEntity {

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    // Đường dẫn khi người dùng click vào banner
    @Column(name = "link_url", columnDefinition = "TEXT")
    private String linkUrl;

    // Thứ tự hiển thị – số nhỏ hiển thị trước
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    // true = đang hiển thị trên trang chủ, false = ẩn
    @Column(name = "active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean active = true;
}
