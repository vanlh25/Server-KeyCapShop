package com.vn.keycap_server.service.category;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vn.keycap_server.dto.request.category.CreateCategoryRequest;
import com.vn.keycap_server.dto.request.category.UpdateCategoryRequest;
import com.vn.keycap_server.dto.response.CategoryResponse;
import com.vn.keycap_server.exception.BadRequestException;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.mapper.CategoryMapper;
import com.vn.keycap_server.modal.Category;
import com.vn.keycap_server.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý nghiệp vụ danh mục sản phẩm.
 */
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = getEntity(id);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String slug = request.getSlug().trim().toLowerCase();
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Slug đã tồn tại: " + slug);
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .build();

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = getEntity(id);

        if (StringUtils.hasText(request.getName())) {
            category.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getSlug())) {
            String slug = request.getSlug().trim().toLowerCase();
            if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
                throw new BadRequestException("Slug đã tồn tại: " + slug);
            }
            category.setSlug(slug);
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getEntity(id);
        categoryRepository.delete(category);
    }

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }
}

