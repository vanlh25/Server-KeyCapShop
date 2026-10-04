package com.vn.keycap_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.keycap_server.modal.Category;

/**
 * Repository thao tác dữ liệu bảng categories.
 * Chỉ giao tiếp với database và không chứa logic nghiệp vụ.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}

