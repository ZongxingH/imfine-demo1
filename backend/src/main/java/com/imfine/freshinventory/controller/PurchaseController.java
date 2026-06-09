package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.DocumentDtos.PurchaseRequest;
import com.imfine.freshinventory.dto.DocumentDtos.PurchaseResponse;
import com.imfine.freshinventory.service.PurchaseService;
import com.imfine.freshinventory.validation.PageRequestFactory;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public PageResponse<PurchaseResponse> page(@RequestParam(required = false) String orderNo,
                                               @RequestParam(required = false) Long supplierId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return purchaseService.page(orderNo, supplierId, fromDate, toDate, PageRequestFactory.of(page, size));
    }

    @GetMapping("/{id}")
    public PurchaseResponse detail(@PathVariable Long id) {
        return purchaseService.detail(id);
    }

    @PostMapping
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request) {
        return purchaseService.create(request);
    }

    @PutMapping("/{id}")
    public PurchaseResponse update(@PathVariable Long id, @Valid @RequestBody PurchaseRequest request) {
        return purchaseService.update(id, request);
    }
}
