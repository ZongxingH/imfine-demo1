package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.DocumentDtos.SalesRequest;
import com.imfine.freshinventory.dto.DocumentDtos.SalesResponse;
import com.imfine.freshinventory.service.SalesService;
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
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public PageResponse<SalesResponse> page(@RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) String customerName,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return salesService.page(orderNo, customerName, fromDate, toDate, PageRequestFactory.of(page, size));
    }

    @GetMapping("/{id}")
    public SalesResponse detail(@PathVariable Long id) {
        return salesService.detail(id);
    }

    @PostMapping
    public SalesResponse create(@Valid @RequestBody SalesRequest request) {
        return salesService.create(request);
    }

    @PutMapping("/{id}")
    public SalesResponse update(@PathVariable Long id, @Valid @RequestBody SalesRequest request) {
        return salesService.update(id, request);
    }
}
