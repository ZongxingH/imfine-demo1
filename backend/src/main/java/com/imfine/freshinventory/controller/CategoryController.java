package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.dto.CategoryDtos.CategoryRequest;
import com.imfine.freshinventory.dto.CategoryDtos.CategoryResponse;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.service.CategoryService;
import com.imfine.freshinventory.validation.PageRequestFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public PageResponse<CategoryResponse> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Status status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return categoryService.page(keyword, status, PageRequestFactory.of(page, size));
    }

    @GetMapping("/{id}")
    public CategoryResponse detail(@PathVariable Long id) {
        return categoryService.detail(id);
    }

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }
}
