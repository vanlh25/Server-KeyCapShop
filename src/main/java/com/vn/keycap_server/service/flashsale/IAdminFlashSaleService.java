package com.vn.keycap_server.service.flashsale;

import java.util.List;

import org.springframework.data.domain.Page;

import com.vn.keycap_server.dto.request.flashsale.AdminListFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.CreateFlashSaleRequest;
import com.vn.keycap_server.dto.request.flashsale.FlashSaleItemRequest;
import com.vn.keycap_server.dto.request.flashsale.UpdateFlashSaleRequest;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleDetailResponse;
import com.vn.keycap_server.dto.response.flashsale.AdminFlashSaleItemResponse;

public interface IAdminFlashSaleService {

    Page<AdminFlashSaleItemResponse> getFlashSales(AdminListFlashSaleRequest request);

    AdminFlashSaleDetailResponse getFlashSaleById(Long id);

    AdminFlashSaleDetailResponse createFlashSale(CreateFlashSaleRequest request);

    AdminFlashSaleDetailResponse updateFlashSale(Long id, UpdateFlashSaleRequest request);

    void cancelFlashSale(Long id);

    AdminFlashSaleDetailResponse addItemsToFlashSale(Long id, List<FlashSaleItemRequest> items);

    void removeItemFromFlashSale(Long flashSaleId, Long itemId);
}
