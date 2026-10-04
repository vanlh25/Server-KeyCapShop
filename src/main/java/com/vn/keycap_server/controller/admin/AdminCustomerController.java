package com.vn.keycap_server.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.PaginationMeta;
import com.vn.keycap_server.dto.request.customer.AdminCustomerListRequest;
import com.vn.keycap_server.dto.request.customer.AdminUpdateCustomerRequest;
import com.vn.keycap_server.dto.response.customer.AdminCustomerResponse;
import com.vn.keycap_server.service.customer.IAdminCustomerService;
import com.vn.keycap_server.utils.PaginationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý khách hàng trong khu vực admin.
 * Chỉ cần ADMIN hoặc STAFF. Quyền tập trung tại SecurityConfig (/admin/**).
 */
@Validated
@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AdminCustomerController {

    private final IAdminCustomerService adminCustomerService;

    /**
     * Lấy danh sách khách hàng có phân trang và tìm kiếm.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getCustomers(@Valid @ModelAttribute AdminCustomerListRequest request) {
        Page<AdminCustomerResponse> customerPage = adminCustomerService.getCustomers(request);
        PaginationMeta meta = PaginationUtils.buildPaginationMeta(customerPage, request.getPage());

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách khách hàng thành công")
                .data(customerPage.getContent())
                .pagination(meta)
                .build());
    }

    /**
     * Lấy chi tiết khách hàng theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCustomerById(@PathVariable Long id) {
        AdminCustomerResponse customer = adminCustomerService.getCustomerById(id);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy thông tin khách hàng thành công")
                .data(customer)
                .build());
    }

    /**
     * Cập nhật trạng thái khoá/mở tài khoản khách hàng.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCustomer(
            @PathVariable Long id,
            @RequestBody AdminUpdateCustomerRequest request) {
        AdminCustomerResponse customer = adminCustomerService.updateCustomer(id, request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật khách hàng thành công")
                .data(customer)
                .build());
    }
}
