package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.ProductCategory;
import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.dto.CategoryDtos.CategoryRequest;
import com.imfine.freshinventory.dto.CategoryDtos.CategoryResponse;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.repository.ProductCategoryRepository;
import com.imfine.freshinventory.validation.ValidationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final ProductCategoryRepository categoryRepository;

    public CategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        ValidationUtils.requireUnique(!categoryRepository.existsByName(request.name()), "Category name already exists");
        ProductCategory category = new ProductCategory();
        apply(category, request);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        ProductCategory category = getEntity(id);
        ValidationUtils.requireUnique(!categoryRepository.existsByNameAndIdNot(request.name(), id), "Category name already exists");
        apply(category, request);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse detail(Long id) {
        return CategoryResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> page(String keyword, Status status, Pageable pageable) {
        return PageResponse.from(categoryRepository.search(blankToNull(keyword), status, pageable).map(CategoryResponse::from));
    }

    ProductCategory getEntity(Long id) {
        return ValidationUtils.requireFound(categoryRepository.findById(id), "Category not found");
    }

    private void apply(ProductCategory category, CategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
        category.setStatus(request.status() == null ? Status.ACTIVE : request.status());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
