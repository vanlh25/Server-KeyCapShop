package com.vn.keycap_server.dto.request.customer;

import lombok.Data;

/**
 * Request để admin cập nhật thông tin khách hàng (chỉ trạng thái khóa/mở).
 */
@Data
public class AdminUpdateCustomerRequest {

    // true = tài khoản bị khóa, false = đang hoạt động bình thường
    private Boolean locked;
}
