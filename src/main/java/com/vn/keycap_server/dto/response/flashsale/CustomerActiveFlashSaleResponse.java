package com.vn.keycap_server.dto.response.flashsale;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActiveFlashSaleResponse {
    private CustomerTimeSlotResponse currentSlot;
    private List<CustomerTimeSlotResponse> activeSlots;
    private List<CustomerTimeSlotResponse> upcomingSlots;
}
