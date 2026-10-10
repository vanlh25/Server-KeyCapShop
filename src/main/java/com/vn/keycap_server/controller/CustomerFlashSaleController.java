package com.vn.keycap_server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerActiveFlashSaleResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerFlashSaleProductResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerTimeSlotResponse;
import com.vn.keycap_server.service.flashsale.ICustomerFlashSaleService;

import lombok.RequiredArgsConstructor;

/**
 * Controller phục vụ người dùng săn Flash Sale (Public APIs).
 */
@RestController
@RequestMapping("/flash-sales")
@RequiredArgsConstructor
public class CustomerFlashSaleController {

    private final ICustomerFlashSaleService customerFlashSaleService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveFlashSale() {
        CustomerActiveFlashSaleResponse response = customerFlashSaleService.getActiveFlashSale();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy thông tin đợt Flash Sale đang diễn ra thành công")
                .data(response)
                .build());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse> getUpcomingSlots() {
        List<CustomerTimeSlotResponse> upcoming = customerFlashSaleService.getUpcomingSlots();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy danh sách các khung giờ Flash Sale sắp tới thành công")
                .data(upcoming)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getFlashSaleById(@PathVariable Long id) {
        CustomerTimeSlotResponse response = customerFlashSaleService.getFlashSaleById(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lấy chi tiết khung giờ Flash Sale thành công")
                .data(response)
                .build());
    }

    @GetMapping("/variants/{variantId}/active")
    public ResponseEntity<ApiResponse> getVariantFlashSale(@PathVariable Long variantId) {
        CustomerFlashSaleProductResponse response = customerFlashSaleService.getVariantFlashSale(variantId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Kiểm tra Flash Sale cho biến thể thành công")
                .data(response)
                .build());
    }
}
