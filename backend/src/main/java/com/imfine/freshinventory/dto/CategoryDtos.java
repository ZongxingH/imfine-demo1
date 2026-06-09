package com.imfine.freshinventory.dto;

import com.imfine.freshinventory.domain.ProductCategory;
import com.imfine.freshinventory.domain.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 255) String description,
            Status status
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String description,
            Status status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CategoryResponse from(ProductCategory category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.getStatus(),
                    category.getCreatedAt(),
                    category.getUpdatedAt());
        }
    }
}
