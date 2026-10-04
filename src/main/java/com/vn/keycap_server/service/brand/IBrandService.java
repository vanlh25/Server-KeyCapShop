package com.vn.keycap_server.service.brand;

import java.util.List;

import com.vn.keycap_server.dto.request.brand.CreateBrandRequest;
import com.vn.keycap_server.dto.request.brand.UpdateBrandRequest;
import com.vn.keycap_server.dto.response.BrandResponse;

/**
 * Contract nghiệp vụ cho thương hiệu.
 * Controller chỉ phụ thuộc vào interface này để giữ đúng phân tầng Controller -> Service.
 */
public interface IBrandService {

    List<BrandResponse> getAllBrands();

    BrandResponse getBrandById(Long id);

    BrandResponse createBrand(CreateBrandRequest request);

    BrandResponse updateBrand(Long id, UpdateBrandRequest request);

    void deleteBrand(Long id);
}
