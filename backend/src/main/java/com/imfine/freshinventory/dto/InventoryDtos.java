package com.imfine.freshinventory.dto;

import com.imfine.freshinventory.domain.MovementType;
import com.imfine.freshinventory.domain.SourceType;
import com.imfine.freshinventory.domain.StockMovement;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class InventoryDtos {
    private InventoryDtos() {
    }

    public record InventoryRow(
            Long productId,
            String productCode,
            String productName,
            Long categoryId,
            String categoryName,
            String unit,
            BigDecimal currentStock
    ) {
    }

    public record InventoryDetail(
            Long productId,
            String productCode,
            String productName,
            String unit,
            BigDecimal currentStock
    ) {
    }

    public record StockMovementResponse(
            Long id,
            Long productId,
            MovementType movementType,
            SourceType sourceType,
            Long sourceId,
            Long sourceItemId,
            BigDecimal quantityDelta,
            LocalDate movementDate,
            Instant createdAt
    ) {
        public static StockMovementResponse from(StockMovement movement) {
            return new StockMovementResponse(
                    movement.getId(),
                    movement.getProduct().getId(),
                    movement.getMovementType(),
                    movement.getSourceType(),
                    movement.getSourceId(),
                    movement.getSourceItemId(),
                    movement.getQuantityDelta(),
                    movement.getMovementDate(),
                    movement.getCreatedAt());
        }
    }

    public record SlowMovingRow(
            Long productId,
            String productCode,
            String productName,
            String categoryName,
            String unit,
            BigDecimal currentStock,
            LocalDate lastSalesDate,
            Long daysSinceLastSale
    ) {
    }
}
