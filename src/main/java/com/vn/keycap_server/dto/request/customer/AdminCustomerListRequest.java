package com.vn.keycap_server.dto.request.customer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request query dùng cho API lấy danh sách khách hàng trong trang quản trị.
 */
@Data
public class AdminCustomerListRequest {

    @Min(value = 1, message = "Trang hiện tại phải lớn hơn hoặc bằng 1")
    private int page = 1;

    @Min(value = 1, message = "Số lượng khách hàng mỗi trang phải lớn hơn hoặc bằng 1")
    @Max(value = 100, message = "Số lượng khách hàng mỗi trang không được vượt quá 100")
    private int limit = 20;

    @Size(max = 100, message = "Từ khóa tìm kiếm không được vượt quá 100 ký tự")
    private String search;
}
