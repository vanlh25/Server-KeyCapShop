package com.vn.keycap_server.service.banner;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.keycap_server.dto.request.banner.CreateBannerRequest;
import com.vn.keycap_server.dto.request.banner.UpdateBannerRequest;
import com.vn.keycap_server.dto.response.banner.BannerResponse;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.modal.Banner;
import com.vn.keycap_server.repository.BannerRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý banner quảng cáo trang chủ.
 */
@Service
@RequiredArgsConstructor
public class BannerService implements IBannerService {

    private final BannerRepository bannerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBanners() {
        return bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse getBannerById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public BannerResponse createBanner(CreateBannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
                .build();
        return toResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long id, UpdateBannerRequest request) {
        Banner banner = getEntity(id);

        if (request.getTitle() != null) banner.setTitle(request.getTitle());
        if (request.getImageUrl() != null) banner.setImageUrl(request.getImageUrl());
        if (request.getLinkUrl() != null) banner.setLinkUrl(request.getLinkUrl());
        if (request.getDisplayOrder() != null) banner.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) banner.setActive(request.getActive());

        return toResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.delete(getEntity(id));
    }

    private Banner getEntity(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
    }

    private BannerResponse toResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .displayOrder(banner.getDisplayOrder())
                .active(banner.isActive())
                .createdAt(banner.getCreatedAt())
                .build();
    }
}
