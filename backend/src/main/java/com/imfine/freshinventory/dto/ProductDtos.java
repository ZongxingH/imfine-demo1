package com.imfine.freshinventory.dto;

import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 60) String code,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 20) String unit,
            @Positive Integer shelfLifeDays,
            @DecimalMin("0.00") BigDecimal suggestedSalePrice,
            Status status
    ) {
    }

    public record ProductResponse(
            Long id,
            Long categoryId,
            String categoryName,
            String code,
            String name,
            String unit,
            Integer shelfLifeDays,
            BigDecimal suggestedSalePrice,
            Status status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCode(),
                    product.getName(),
                    product.getUnit(),
                    product.getShelfLifeDays(),
                    product.getSuggestedSalePrice(),
                    product.getStatus(),
                    product.getCreatedAt(),
                    product.getUpdatedAt());
        }
    }
}
