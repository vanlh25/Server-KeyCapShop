package com.vn.keycap_server.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.keycap_server.dto.ApiResponse;
import com.vn.keycap_server.dto.PaginationMeta;
import com.vn.keycap_server.dto.request.flashsale.AdminListFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.CreateFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.FlashSaleItemRequest;
import com.vn.keycap_server.dto.request.flashsale.UpdateFlashSaleRequest;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleDetailResponse;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleItemResponse;
import com.vn.keycap_server.service.flashsale.IAdminFlashSaleService;
import com.vn.keycap_server.utils.PaginationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/admin/flash-sales")
@RequiredArgsConstructor
public class AdminFlashSaleController {

        private final IAdminFlashSaleService adminFlashSaleService;

        @GetMapping
        public ResponseEntity<ApiResponse> getFlashSales(@Valid @ModelAttribute AdminListFlashSaleRequest request) {
                Page<AdminFlashSaleItemResponse> resultPage = adminFlashSaleService.getFlashSales(request);
                PaginationMeta meta = PaginationUtils.buildPaginationMeta(resultPage, request.getPage());

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Lấy danh sách chiến dịch Flash Sale thành công")
                                .data(resultPage.getContent())
                                .pagination(meta)
                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse> getFlashSaleById(@PathVariable Long id) {
                AdminFlashSaleDetailResponse detail = adminFlashSaleService.getFlashSaleById(id);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Lấy chi tiết chiến dịch Flash Sale thành công")
                                .data(detail)
                                .build());
        }

        @PostMapping
        public ResponseEntity<ApiResponse> createFlashSale(@Valid @RequestBody CreateFlashSaleRequest request) {
                AdminFlashSaleDetailResponse created = adminFlashSaleService.createFlashSale(request);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Tạo chiến dịch Flash Sale thành công")
                                .data(created)
                                .build());
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse> updateFlashSale(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateFlashSaleRequest request) {
                AdminFlashSaleDetailResponse updated = adminFlashSaleService.updateFlashSale(id, request);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Cập nhật chiến dịch Flash Sale thành công")
                                .data(updated)
                                .build());
        }

        @PatchMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse> cancelFlashSale(@PathVariable Long id) {
                adminFlashSaleService.cancelFlashSale(id);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Hủy chiến dịch Flash Sale thành công")
                                .data(null)
                                .build());
        }

        @PostMapping("/{id}/items")
        public ResponseEntity<ApiResponse> addItems(
                        @PathVariable Long id,
                        @Valid @RequestBody List<FlashSaleItemRequest> items) {
                AdminFlashSaleDetailResponse updated = adminFlashSaleService.addItemsToFlashSale(id, items);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Thêm sản phẩm vào chiến dịch Flash Sale thành công")
                                .data(updated)
                                .build());
        }

        @DeleteMapping("/{id}/items/{itemId}")
        public ResponseEntity<ApiResponse> removeItem(
                        @PathVariable Long id,
                        @PathVariable Long itemId) {
                adminFlashSaleService.removeItemFromFlashSale(id, itemId);

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Xóa sản phẩm khỏi chiến dịch Flash Sale thành công")
                                .data(null)
                                .build());
        }
}
