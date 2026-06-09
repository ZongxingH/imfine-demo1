package com.imfine.freshinventory.dto;

import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.domain.Supplier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class SupplierDtos {
    private SupplierDtos() {
    }

    public record SupplierRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 80) String contactName,
            @Pattern(regexp = "^$|^[0-9+()\\- ]{6,40}$", message = "must be a valid phone number") String contactPhone,
            @Size(max = 255) String address,
            Status status
    ) {
    }

    public record SupplierResponse(
            Long id,
            String name,
            String contactName,
            String contactPhone,
            String address,
            Status status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static SupplierResponse from(Supplier supplier) {
            return new SupplierResponse(
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getContactName(),
                    supplier.getContactPhone(),
                    supplier.getAddress(),
                    supplier.getStatus(),
                    supplier.getCreatedAt(),
                    supplier.getUpdatedAt());
        }
    }
}
