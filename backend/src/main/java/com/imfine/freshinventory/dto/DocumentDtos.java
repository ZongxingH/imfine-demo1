package com.imfine.freshinventory.dto;

import com.imfine.freshinventory.domain.PurchaseOrder;
import com.imfine.freshinventory.domain.PurchaseOrderItem;
import com.imfine.freshinventory.domain.SalesOrder;
import com.imfine.freshinventory.domain.SalesOrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class DocumentDtos {
    private DocumentDtos() {
    }

    public record PurchaseRequest(
            @NotBlank @Size(max = 60) String orderNo,
            @NotNull Long supplierId,
            @NotNull @PastOrPresent LocalDate inboundDate,
            @Size(max = 255) String remark,
            @NotEmpty @Valid List<PurchaseItemRequest> items
    ) {
    }

    public record PurchaseItemRequest(
            @NotNull Long productId,
            @NotNull @DecimalMin(value = "0.000", inclusive = false) BigDecimal quantity,
            @NotNull @DecimalMin("0.00") BigDecimal unitCost
    ) {
    }

    public record PurchaseResponse(
            Long id,
            String orderNo,
            Long supplierId,
            String supplierName,
            LocalDate inboundDate,
            BigDecimal totalAmount,
            String remark,
            List<PurchaseItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PurchaseResponse from(PurchaseOrder order) {
            return new PurchaseResponse(
                    order.getId(),
                    order.getOrderNo(),
                    order.getSupplier().getId(),
                    order.getSupplier().getName(),
                    order.getInboundDate(),
                    order.getTotalAmount(),
                    order.getRemark(),
                    order.getItems().stream().map(PurchaseItemResponse::from).toList(),
                    order.getCreatedAt(),
                    order.getUpdatedAt());
        }
    }

    public record PurchaseItemResponse(
            Long id,
            Long productId,
            String productCode,
            String productName,
            BigDecimal quantity,
            BigDecimal unitCost,
            BigDecimal amount
    ) {
        static PurchaseItemResponse from(PurchaseOrderItem item) {
            return new PurchaseItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getCode(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitCost(),
                    item.getAmount());
        }
    }

    public record SalesRequest(
            @NotBlank @Size(max = 60) String orderNo,
            @Size(max = 120) String customerName,
            @NotNull @PastOrPresent LocalDate outboundDate,
            @Size(max = 255) String remark,
            @NotEmpty @Valid List<SalesItemRequest> items
    ) {
    }

    public record SalesItemRequest(
            @NotNull Long productId,
            @NotNull @DecimalMin(value = "0.000", inclusive = false) BigDecimal quantity,
            @NotNull @DecimalMin("0.00") BigDecimal unitPrice
    ) {
    }

    public record SalesResponse(
            Long id,
            String orderNo,
            String customerName,
            LocalDate outboundDate,
            BigDecimal totalAmount,
            String remark,
            List<SalesItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static SalesResponse from(SalesOrder order) {
            return new SalesResponse(
                    order.getId(),
                    order.getOrderNo(),
                    order.getCustomerName(),
                    order.getOutboundDate(),
                    order.getTotalAmount(),
                    order.getRemark(),
                    order.getItems().stream().map(SalesItemResponse::from).toList(),
                    order.getCreatedAt(),
                    order.getUpdatedAt());
        }
    }

    public record SalesItemResponse(
            Long id,
            Long productId,
            String productCode,
            String productName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amount
    ) {
        static SalesItemResponse from(SalesOrderItem item) {
            return new SalesItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getCode(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getAmount());
        }
    }
}
