package com.vn.keycap_server.service.customer;

import org.springframework.data.domain.Page;

import com.vn.keycap_server.dto.request.customer.AdminCustomerListRequest;
import com.vn.keycap_server.dto.request.customer.AdminUpdateCustomerRequest;
import com.vn.keycap_server.dto.response.customer.AdminCustomerResponse;

/**
 * Contract nghiệp vụ quản lý khách hàng dành cho Admin.
 */
public interface IAdminCustomerService {

    Page<AdminCustomerResponse> getCustomers(AdminCustomerListRequest request);

    AdminCustomerResponse getCustomerById(Long customerId);

    AdminCustomerResponse updateCustomer(Long customerId, AdminUpdateCustomerRequest request);
}
