package com.vn.keycap_server.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vn.keycap_server.utils.EGender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin khách hàng cho Admin.
 * Không lộ password. Kèm thống kê tổng đơn và tổng chi tiêu.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCustomerResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private EGender gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private boolean locked;
    private LocalDate createdAt;

    // Thống kê nghiệp vụ
    private long totalOrders;
    private BigDecimal totalSpent;
}
