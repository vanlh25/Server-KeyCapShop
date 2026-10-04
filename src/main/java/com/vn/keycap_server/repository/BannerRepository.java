package com.vn.keycap_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.Banner;

/**
 * Repository thao tác dữ liệu bảng banners.
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    // Lấy banner đang active, sắp xếp theo displayOrder để slider hiển thị đúng thứ tự
    List<Banner> findByActiveTrueOrderByDisplayOrderAsc();
}
