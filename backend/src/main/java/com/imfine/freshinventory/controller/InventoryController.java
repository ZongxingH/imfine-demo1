package com.imfine.freshinventory.controller;

import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.InventoryDtos.InventoryDetail;
import com.imfine.freshinventory.dto.InventoryDtos.InventoryRow;
import com.imfine.freshinventory.dto.InventoryDtos.StockMovementResponse;
import com.imfine.freshinventory.service.InventoryService;
import com.imfine.freshinventory.validation.PageRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public PageResponse<InventoryRow> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return inventoryService.page(keyword, categoryId, PageRequestFactory.of(page, size));
    }

    @GetMapping("/products/{productId}")
    public InventoryDetail detail(@PathVariable Long productId) {
        return inventoryService.detail(productId);
    }

    @GetMapping("/products/{productId}/movements")
    public PageResponse<StockMovementResponse> movements(@PathVariable Long productId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return inventoryService.movements(productId, PageRequestFactory.of(page, size));
    }
}
