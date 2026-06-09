package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.SupplierDtos.SupplierRequest;
import com.imfine.freshinventory.dto.SupplierDtos.SupplierResponse;
import com.imfine.freshinventory.service.SupplierService;
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
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public PageResponse<SupplierResponse> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Status status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return supplierService.page(keyword, status, PageRequestFactory.of(page, size));
    }

    @GetMapping("/{id}")
    public SupplierResponse detail(@PathVariable Long id) {
        return supplierService.detail(id);
    }

    @PostMapping
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }
}
