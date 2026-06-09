package com.imfine.freshinventory.service;

import com.imfine.freshinventory.domain.MovementType;
import com.imfine.freshinventory.domain.Product;
import com.imfine.freshinventory.domain.SalesOrder;
import com.imfine.freshinventory.domain.SalesOrderItem;
import com.imfine.freshinventory.domain.SourceType;
import com.imfine.freshinventory.domain.Status;
import com.imfine.freshinventory.dto.CommonDtos.PageResponse;
import com.imfine.freshinventory.dto.DocumentDtos.SalesItemRequest;
import com.imfine.freshinventory.dto.DocumentDtos.SalesRequest;
import com.imfine.freshinventory.dto.DocumentDtos.SalesResponse;
import com.imfine.freshinventory.exception.ErrorCode;
import com.imfine.freshinventory.repository.SalesOrderRepository;
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
public class SalesService {
    private final SalesOrderRepository salesOrderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public SalesService(SalesOrderRepository salesOrderRepository, ProductService productService, InventoryService inventoryService) {
        this.salesOrderRepository = salesOrderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public SalesResponse create(SalesRequest request) {
        ValidationUtils.requireUnique(!salesOrderRepository.existsByOrderNo(request.orderNo()), "Sales order number already exists");
        validateStock(request);
        SalesOrder order = new SalesOrder();
        apply(order, request);
        SalesOrder saved = salesOrderRepository.saveAndFlush(order);
        writeMovements(saved);
        return SalesResponse.from(saved);
    }

    @Transactional
    public SalesResponse update(Long id, SalesRequest request) {
        SalesOrder order = getEntity(id);
        ValidationUtils.requireUnique(!salesOrderRepository.existsByOrderNoAndIdNot(request.orderNo(), id), "Sales order number already exists");
        inventoryService.replaceSourceMovements(SourceType.SALES_ORDER, id);
        validateStock(request);
        order.clearItems();
        apply(order, request);
        SalesOrder saved = salesOrderRepository.saveAndFlush(order);
        writeMovements(saved);
        return SalesResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SalesResponse detail(Long id) {
        return SalesResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SalesResponse> page(String orderNo, String customerName, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return PageResponse.from(salesOrderRepository.search(blankToNull(orderNo), blankToNull(customerName), fromDate, toDate, pageable)
                .map(SalesResponse::from));
    }

    private SalesOrder getEntity(Long id) {
        return ValidationUtils.requireFound(salesOrderRepository.findById(id), "Sales order not found");
    }

    private void validateStock(SalesRequest request) {
        Map<Long, BigDecimal> requestedByProduct = new LinkedHashMap<>();
        for (SalesItemRequest item : request.items()) {
            requestedByProduct.merge(item.productId(), item.quantity(), BigDecimal::add);
        }
        requestedByProduct.forEach(inventoryService::requireAvailable);
    }

    private void apply(SalesOrder order, SalesRequest request) {
        order.setOrderNo(request.orderNo());
        order.setCustomerName(request.customerName());
        order.setOutboundDate(request.outboundDate());
        order.setRemark(request.remark());
        BigDecimal total = BigDecimal.ZERO;
        for (SalesItemRequest itemRequest : request.items()) {
            Product product = productService.getEntity(itemRequest.productId());
            ValidationUtils.requireTrue(product.getStatus() == Status.ACTIVE, ErrorCode.VALIDATION_ERROR, "Inactive product cannot be used");
            SalesOrderItem item = new SalesOrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setAmount(money(itemRequest.quantity().multiply(itemRequest.unitPrice())));
            total = total.add(item.getAmount());
            order.addItem(item);
        }
        order.setTotalAmount(money(total));
    }

    private void writeMovements(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            inventoryService.saveMovement(
                    item.getProduct(),
                    MovementType.SALES_OUT,
                    SourceType.SALES_ORDER,
                    order.getId(),
                    item.getId(),
                    item.getQuantity().negate(),
                    order.getOutboundDate());
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
