package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.InventoryDtos.SlowMovingRow;
import com.imfine.freshinventory.service.InventoryService;
import com.imfine.freshinventory.validation.PageRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final InventoryService inventoryService;

    public ReportController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/slow-moving-products")
    public PageResponse<SlowMovingRow> slowMovingProducts(@RequestParam(defaultValue = "30") int days,
                                                          @RequestParam(required = false) Long categoryId,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        PageRequestFactory.of(page, size);
        return inventoryService.slowMovingProducts(days, categoryId, keyword, page, size);
    }
}
