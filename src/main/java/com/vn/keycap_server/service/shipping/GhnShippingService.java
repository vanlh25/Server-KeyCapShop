package com.vn.keycap_server.service.shipping;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.vn.keycap_server.client.GhnFeignClient;
import com.vn.keycap_server.configuration.ghn.GhnProperties;
import com.vn.keycap_server.dto.request.shipping.GhnFeeRequest;
import com.vn.keycap_server.dto.response.shipping.GhnFeeResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.modal.Address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnShippingService implements IShippingService {
    private final GhnFeignClient ghnFeignClient;
    private final GhnProperties ghnProperties;

    @Override
    public BigDecimal calculateShippingFee(Address address, int totalWeight) {
        // ponytail: fallback 30k fee if GHN sandbox/token unreachable; upgrade to retry/circuit-breaker for prod
        try {
            if (address != null && address.getDistrictCode() != null && address.getWardCode() != null) {
                GhnFeeRequest request = GhnFeeRequest.builder()
                        .service_type_id(2)
                        .from_district_id(ghnProperties.getFromDistrictId())
                        .from_ward_code(ghnProperties.getFromWardCode())
                        .to_district_id(Integer.parseInt(address.getDistrictCode().trim()))
                        .to_ward_code(address.getWardCode().trim())
                        .weight(totalWeight)
                        .build();
                GhnFeeResponse response = ghnFeignClient.getShippingFee(
                        ghnProperties.getToken(), ghnProperties.getShopId(), request);
                if (response != null && response.getCode() == 200 && response.getData() != null) {
                    return BigDecimal.valueOf(response.getData().getTotal());
                }
            }
        } catch (Exception e) {
            log.warn("GHN fee lookup failed: {}, using fallback 30000", e.getMessage());
        }
        return BigDecimal.valueOf(30000);
    }

}

