package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.MovementType;
import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.SourceType;
import com.imfine.freshinventory.domain.StockMovement;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.InventoryDtos.InventoryDetail;
import com.imfine.freshinventory.dto.InventoryDtos.InventoryRow;
import com.imfine.freshinventory.dto.InventoryDtos.SlowMovingRow;
import com.imfine.freshinventory.dto.InventoryDtos.StockMovementResponse;
import com.imfine.freshinventory.exception.ErrorCode;
import com.imfine.freshinventory.repository.ProductRepository;
import com.imfine.freshinventory.repository.StockMovementRepository;
import com.imfine.freshinventory.validation.ValidationUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public InventoryService(StockMovementRepository stockMovementRepository, ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void saveMovement(Product product, MovementType movementType, SourceType sourceType, Long sourceId,
                             Long sourceItemId, BigDecimal quantityDelta, LocalDate movementDate) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setSourceType(sourceType);
        movement.setSourceId(sourceId);
        movement.setSourceItemId(sourceItemId);
        movement.setQuantityDelta(quantityDelta);
        movement.setMovementDate(movementDate);
        stockMovementRepository.save(movement);
    }

    @Transactional
    public void replaceSourceMovements(SourceType sourceType, Long sourceId) {
        stockMovementRepository.deleteBySource(sourceType, sourceId);
        stockMovementRepository.flush();
    }

    @Transactional(readOnly = true)
    public BigDecimal currentStock(Long productId) {
        BigDecimal stock = stockMovementRepository.currentStock(productId);
        return stock == null ? BigDecimal.ZERO : stock;
    }

    @Transactional(readOnly = true)
    public void requireAvailable(Long productId, BigDecimal requestedQuantity) {
        BigDecimal available = currentStock(productId);
        ValidationUtils.requireTrue(available.compareTo(requestedQuantity) >= 0,
                ErrorCode.INSUFFICIENT_STOCK,
                "Insufficient stock for product " + productId + ": available " + available + ", requested " + requestedQuantity);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryRow> page(String keyword, Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.search(blankToNull(keyword), categoryId, null, pageable);
        return PageResponse.from(products.map(this::toInventoryRow));
    }

    @Transactional(readOnly = true)
    public InventoryDetail detail(Long productId) {
        Product product = ValidationUtils.requireFound(productRepository.findDetailById(productId), "Product not found");
        return new InventoryDetail(product.getId(), product.getCode(), product.getName(), product.getUnit(), currentStock(productId));
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> movements(Long productId, Pageable pageable) {
        ValidationUtils.requireFound(productRepository.findById(productId), "Product not found");
        return PageResponse.from(stockMovementRepository.findByProductId(productId, pageable).map(StockMovementResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<SlowMovingRow> slowMovingProducts(int days, Long categoryId, String keyword, int page, int size) {
        ValidationUtils.requireTrue(days > 0, ErrorCode.VALIDATION_ERROR, "days must be positive");
        Pageable all = org.springframework.data.domain.Pageable.unpaged();
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.minusDays(days);
        List<SlowMovingRow> rows = productRepository.search(blankToNull(keyword), categoryId, null, all).stream()
                .map(product -> toSlowMovingRow(product, today, cutoff))
                .filter(row -> row.currentStock().compareTo(BigDecimal.ZERO) > 0)
                .filter(row -> row.lastSalesDate() == null || row.lastSalesDate().isBefore(cutoff))
                .toList();
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        return PageResponse.of(rows.subList(from, to), page, size, rows.size());
    }

    private InventoryRow toInventoryRow(Product product) {
        return new InventoryRow(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getUnit(),
                currentStock(product.getId()));
    }

    private SlowMovingRow toSlowMovingRow(Product product, LocalDate today, LocalDate cutoff) {
        LocalDate lastSale = stockMovementRepository.lastMovementDate(product.getId(), MovementType.SALES_OUT);
        Long daysSinceLastSale = lastSale == null ? null : ChronoUnit.DAYS.between(lastSale, today);
        return new SlowMovingRow(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getCategory().getName(),
                product.getUnit(),
                currentStock(product.getId()),
                lastSale,
                daysSinceLastSale);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
