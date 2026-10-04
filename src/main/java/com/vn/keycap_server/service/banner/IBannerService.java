package com.vn.keycap_server.service.banner;

import java.util.List;

import com.vn.keycap_server.dto.request.banner.CreateBannerRequest;
import com.vn.keycap_server.dto.request.banner.UpdateBannerRequest;
import com.vn.keycap_server.dto.response.banner.BannerResponse;

/**
 * Contract nghiệp vụ quản lý banner quảng cáo trang chủ.
 */
public interface IBannerService {

    List<BannerResponse> getAllBanners();

    /** Lấy banner đang active để render slider trang chủ (public). */
    List<BannerResponse> getActiveBanners();

    BannerResponse getBannerById(Long id);

    BannerResponse createBanner(CreateBannerRequest request);

    BannerResponse updateBanner(Long id, UpdateBannerRequest request);

    void deleteBanner(Long id);
}
