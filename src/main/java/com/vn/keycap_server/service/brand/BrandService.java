package com.vn.keycap_server.service.brand;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vn.keycap_server.dto.request.brand.CreateBrandRequest;
import com.vn.keycap_server.dto.request.brand.UpdateBrandRequest;
import com.vn.keycap_server.dto.response.BrandResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.mapper.BrandMapper;
import com.vn.keycap_server.modal.Brand;
import com.vn.keycap_server.repository.BrandRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý nghiệp vụ thương hiệu.
 */
@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return brandMapper.toBrandResponseList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        return brandMapper.toBrandResponse(getEntity(id));
    }

    @Override
    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String slug = request.getSlug().trim().toLowerCase();
        if (brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Slug đã tồn tại: " + slug);
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, UpdateBrandRequest request) {
        Brand brand = getEntity(id);

        if (StringUtils.hasText(request.getName())) {
            brand.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getSlug())) {
            String slug = request.getSlug().trim().toLowerCase();
            if (brandRepository.existsBySlugAndIdNot(slug, id)) {
                throw new BadRequestException("Slug đã tồn tại: " + slug);
            }
            brand.setSlug(slug);
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            brand.setImageUrl(request.getImageUrl());
        }

        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        brandRepository.delete(getEntity(id));
    }

    private Brand getEntity(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
    }
}

