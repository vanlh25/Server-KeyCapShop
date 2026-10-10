package com.vn.keycap_server.service.flashsale;

import java.util.List;

import com.vn.keycap_server.dto.response.flashsale.CustomerActiveFlashSaleResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerFlashSaleProductResponse;
import com.vn.keycap_server.dto.response.flashsale.CustomerTimeSlotResponse;

public interface ICustomerFlashSaleService {

    CustomerActiveFlashSaleResponse getActiveFlashSale();

    List<CustomerTimeSlotResponse> getUpcomingSlots();

    CustomerTimeSlotResponse getFlashSaleById(Long id);

    CustomerFlashSaleProductResponse getVariantFlashSale(Long variantId);
}
