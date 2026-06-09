package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.ProductCategory;
import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.ProductDtos.ProductRequest;
import com.imfine.freshinventory.dto.ProductDtos.ProductResponse;
import com.imfine.freshinventory.exception.ErrorCode;
import com.imfine.freshinventory.repository.ProductRepository;
import com.imfine.freshinventory.validation.ValidationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        ValidationUtils.requireUnique(!productRepository.existsByCode(request.code()), "Product code already exists");
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        ValidationUtils.requireUnique(!productRepository.existsByCodeAndIdNot(request.code(), id), "Product code already exists");
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse detail(Long id) {
        return ProductResponse.from(ValidationUtils.requireFound(productRepository.findDetailById(id), "Product not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> page(String keyword, Long categoryId, Status status, Pageable pageable) {
        return PageResponse.from(productRepository.search(blankToNull(keyword), categoryId, status, pageable).map(ProductResponse::from));
    }

    Product getEntity(Long id) {
        return ValidationUtils.requireFound(productRepository.findById(id), "Product not found");
    }

    private void apply(Product product, ProductRequest request) {
        ProductCategory category = categoryService.getEntity(request.categoryId());
        if (request.status() == null || request.status() == Status.ACTIVE) {
            ValidationUtils.requireTrue(category.getStatus() == Status.ACTIVE, ErrorCode.VALIDATION_ERROR, "Active product requires active category");
        }
        product.setCategory(category);
        product.setCode(request.code());
        product.setName(request.name());
        product.setUnit(request.unit());
        product.setShelfLifeDays(request.shelfLifeDays());
        product.setSuggestedSalePrice(request.suggestedSalePrice());
        product.setStatus(request.status() == null ? Status.ACTIVE : request.status());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
