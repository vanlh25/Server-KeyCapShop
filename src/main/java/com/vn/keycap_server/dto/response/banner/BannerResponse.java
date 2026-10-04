package com.vn.keycap_server.dto.response.banner;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin banner quảng cáo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BannerResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private int displayOrder;
    private boolean active;
    private LocalDate createdAt;
}
