package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.MovementType;
import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.PurchaseOrder;
import com.imfine.freshinventory.domain.PurchaseOrderItem;
import com.imfine.freshinventory.domain.SourceType;
import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.domain.Supplier;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.DocumentDtos.PurchaseItemRequest;
import com.imfine.freshinventory.dto.DocumentDtos.PurchaseRequest;
import com.imfine.freshinventory.dto.DocumentDtos.PurchaseResponse;
import com.imfine.freshinventory.exception.ErrorCode;
import com.imfine.freshinventory.repository.PurchaseOrderRepository;
import com.imfine.freshinventory.validation.ValidationUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public PurchaseService(PurchaseOrderRepository purchaseOrderRepository, SupplierService supplierService,
                           ProductService productService, InventoryService inventoryService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierService = supplierService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        ValidationUtils.requireUnique(!purchaseOrderRepository.existsByOrderNo(request.orderNo()), "Purchase order number already exists");
        PurchaseOrder order = new PurchaseOrder();
        apply(order, request);
        PurchaseOrder saved = purchaseOrderRepository.saveAndFlush(order);
        writeMovements(saved);
        return PurchaseResponse.from(saved);
    }

    @Transactional
    public PurchaseResponse update(Long id, PurchaseRequest request) {
        PurchaseOrder order = getEntity(id);
        ValidationUtils.requireUnique(!purchaseOrderRepository.existsByOrderNoAndIdNot(request.orderNo(), id), "Purchase order number already exists");
        validateStockAfterReplacement(order, request);
        inventoryService.replaceSourceMovements(SourceType.PURCHASE_ORDER, id);
        order.clearItems();
        apply(order, request);
        PurchaseOrder saved = purchaseOrderRepository.saveAndFlush(order);
        writeMovements(saved);
        return PurchaseResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseResponse detail(Long id) {
        return PurchaseResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseResponse> page(String orderNo, Long supplierId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return PageResponse.from(purchaseOrderRepository.search(blankToNull(orderNo), supplierId, fromDate, toDate, pageable)
                .map(PurchaseResponse::from));
    }

    private PurchaseOrder getEntity(Long id) {
        return ValidationUtils.requireFound(purchaseOrderRepository.findById(id), "Purchase order not found");
    }

    private void apply(PurchaseOrder order, PurchaseRequest request) {
        Supplier supplier = supplierService.getEntity(request.supplierId());
        ValidationUtils.requireTrue(supplier.getStatus() == Status.ACTIVE, ErrorCode.VALIDATION_ERROR, "Inactive supplier cannot be used");
        order.setOrderNo(request.orderNo());
        order.setSupplier(supplier);
        order.setInboundDate(request.inboundDate());
        order.setRemark(request.remark());
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItemRequest itemRequest : request.items()) {
            Product product = productService.getEntity(itemRequest.productId());
            ValidationUtils.requireTrue(product.getStatus() == Status.ACTIVE, ErrorCode.VALIDATION_ERROR, "Inactive product cannot be used");
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitCost(itemRequest.unitCost());
            item.setAmount(money(itemRequest.quantity().multiply(itemRequest.unitCost())));
            total = total.add(item.getAmount());
            order.addItem(item);
        }
        order.setTotalAmount(money(total));
    }

    private void writeMovements(PurchaseOrder order) {
        for (PurchaseOrderItem item : order.getItems()) {
            inventoryService.saveMovement(
                    item.getProduct(),
                    MovementType.PURCHASE_IN,
                    SourceType.PURCHASE_ORDER,
                    order.getId(),
                    item.getId(),
                    item.getQuantity(),
                    order.getInboundDate());
        }
    }

    private void validateStockAfterReplacement(PurchaseOrder order, PurchaseRequest request) {
        Map<Long, BigDecimal> oldQuantities = new LinkedHashMap<>();
        for (PurchaseOrderItem item : order.getItems()) {
            oldQuantities.merge(item.getProduct().getId(), item.getQuantity(), BigDecimal::add);
        }

        Map<Long, BigDecimal> newQuantities = new LinkedHashMap<>();
        for (PurchaseItemRequest item : request.items()) {
            newQuantities.merge(item.productId(), item.quantity(), BigDecimal::add);
        }

        oldQuantities.forEach((productId, quantity) -> newQuantities.putIfAbsent(productId, BigDecimal.ZERO));
        newQuantities.forEach((productId, newQuantity) -> {
            BigDecimal oldQuantity = oldQuantities.getOrDefault(productId, BigDecimal.ZERO);
            BigDecimal projectedStock = inventoryService.currentStock(productId).subtract(oldQuantity).add(newQuantity);
            ValidationUtils.requireTrue(projectedStock.compareTo(BigDecimal.ZERO) >= 0,
                    ErrorCode.INSUFFICIENT_STOCK,
                    "Purchase edit would leave product " + productId + " with negative stock");
        });
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
