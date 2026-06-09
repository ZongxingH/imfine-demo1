package com.imfine.freshinventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imfine.freshinventory.domain.Status;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FreshInventoryApiTest {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supplierApiSupportsCrudPaginationValidationAndDuplicateErrors() throws Exception {
        String suffix = nextSuffix();
        Long supplierId = createSupplier("Supplier " + suffix, Status.ACTIVE);

        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supplier " + suffix));

        mockMvc.perform(put("/api/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Supplier Updated " + suffix,
                                "contactName", "Buyer",
                                "contactPhone", "13800000000",
                                "address", "Shanghai",
                                "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supplier Updated " + suffix));

        mockMvc.perform(get("/api/suppliers")
                        .param("keyword", "Updated " + suffix)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Supplier Updated " + suffix, "status", "ACTIVE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "", "contactPhone", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void categoryAndProductApisSupportCrudPaginationAndBusinessErrors() throws Exception {
        String suffix = nextSuffix();
        Long categoryId = createCategory("Fruit " + suffix, Status.ACTIVE);
        Long productId = createProduct(categoryId, "APPLE-" + suffix, "Apple " + suffix, Status.ACTIVE);

        mockMvc.perform(get("/api/categories").param("keyword", "Fruit " + suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fruit " + suffix));

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Fresh Fruit " + suffix, "description", "updated", "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fresh Fruit " + suffix));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("APPLE-" + suffix));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "APPLE-" + suffix)
                        .param("categoryId", categoryId.toString())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryId").value(categoryId));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(productPayload(categoryId, "APPLE-" + suffix, "Duplicate " + suffix, Status.ACTIVE))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(productPayload(categoryId, "APPLE-EDIT-" + suffix, "Apple Edited " + suffix, Status.ACTIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("APPLE-EDIT-" + suffix))
                .andExpect(jsonPath("$.name").value("Apple Edited " + suffix));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(productPayload(999999L, "BAD-" + suffix, "Bad " + suffix, Status.ACTIVE))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void purchaseInboundCreatesEditsPagesAndReplacesInventoryMovements() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        Long purchaseId = createPurchase("PO-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.valueOf(100), LocalDate.now().minusDays(1));

        assertCurrentStock(fixture.productId(), "100.000");

        mockMvc.perform(put("/api/purchases/{id}", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload("PO-EDIT-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                                BigDecimal.valueOf(150), LocalDate.now().minusDays(1)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(780.0));

        assertCurrentStock(fixture.productId(), "150.000");

        mockMvc.perform(get("/api/purchases/{id}", purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value(org.hamcrest.Matchers.startsWith("PO-EDIT-")))
                .andExpect(jsonPath("$.items[0].productId").value(fixture.productId()));

        mockMvc.perform(get("/api/purchases").param("orderNo", "PO-EDIT").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/inventory/products/{productId}/movements", fixture.productId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload("PO-BAD-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                                BigDecimal.ZERO, LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void purchaseEditRejectsReductionThatWouldMakeStockNegativeAfterSales() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        Long purchaseId = createPurchase("PO-REDUCE-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.valueOf(100), LocalDate.now().minusDays(2));
        createSale("SO-REDUCE-" + nextSuffix(), fixture.productId(), BigDecimal.valueOf(80), LocalDate.now().minusDays(1));

        assertCurrentStock(fixture.productId(), "20.000");

        mockMvc.perform(put("/api/purchases/{id}", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload("PO-REDUCE-EDIT-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                                BigDecimal.valueOf(50), LocalDate.now().minusDays(2)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        assertCurrentStock(fixture.productId(), "20.000");
    }

    @Test
    void purchaseRejectsDuplicateOrderNumber() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        String orderNo = "PO-DUP-" + nextSuffix();
        createPurchase(orderNo, fixture.supplierId(), fixture.productId(), BigDecimal.TEN, LocalDate.now());

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload(orderNo, fixture.supplierId(), fixture.productId(),
                                BigDecimal.ONE, LocalDate.now()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void purchaseRejectsInactiveSupplier() throws Exception {
        String suffix = nextSuffix();
        Long inactiveSupplierId = createSupplier("Inactive Supplier " + suffix, Status.INACTIVE);
        Long categoryId = createCategory("Inactive Supplier Category " + suffix, Status.ACTIVE);
        Long productId = createProduct(categoryId, "IN-PROD-" + suffix, "Inactive Check " + suffix, Status.ACTIVE);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload("PO-INACTIVE-" + suffix, inactiveSupplierId, productId,
                                BigDecimal.TEN, LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void purchaseRejectsInactiveProduct() throws Exception {
        String suffix = nextSuffix();
        Long supplierId = createSupplier("Inactive Product Supplier " + suffix, Status.ACTIVE);
        Long categoryId = createCategory("Inactive Product Category " + suffix, Status.ACTIVE);
        Long productId = createProduct(categoryId, "PO-INACTIVE-PROD-" + suffix, "Inactive Product " + suffix, Status.INACTIVE);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload("PO-INACTIVE-PROD-" + suffix, supplierId, productId,
                                BigDecimal.TEN, LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void salesOutboundMutatesInventoryEditsMovementsAndRejectsInsufficientStock() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        createPurchase("PO-STOCK-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.valueOf(100), LocalDate.now().minusDays(2));

        Long salesId = createSale("SO-" + nextSuffix(), fixture.productId(), BigDecimal.valueOf(30), LocalDate.now());
        assertCurrentStock(fixture.productId(), "70.000");

        mockMvc.perform(put("/api/sales/{id}", salesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(salesPayload("SO-EDIT-" + nextSuffix(), fixture.productId(), BigDecimal.valueOf(40), LocalDate.now()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(340.0));

        assertCurrentStock(fixture.productId(), "60.000");

        mockMvc.perform(get("/api/sales/{id}", salesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value(org.hamcrest.Matchers.startsWith("SO-EDIT-")))
                .andExpect(jsonPath("$.items[0].productId").value(fixture.productId()));

        mockMvc.perform(get("/api/sales").param("orderNo", "SO-EDIT").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/inventory/products/{productId}/movements", fixture.productId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(salesPayload("SO-BAD-" + nextSuffix(), fixture.productId(), BigDecimal.valueOf(999), LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void salesRejectsDuplicateOrderNumber() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        createPurchase("PO-SO-DUP-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.TEN, LocalDate.now().minusDays(1));
        String orderNo = "SO-DUP-" + nextSuffix();
        createSale(orderNo, fixture.productId(), BigDecimal.ONE, LocalDate.now());

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(salesPayload(orderNo, fixture.productId(), BigDecimal.ONE, LocalDate.now()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void salesRejectsInactiveProduct() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        createPurchase("PO-INACTIVE-SALE-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.TEN, LocalDate.now().minusDays(1));

        mockMvc.perform(put("/api/products/{id}", fixture.productId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(productPayload(fixture.categoryId(), fixture.productCode(), "Inactive Sale Product", Status.INACTIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(salesPayload("SO-INACTIVE-PROD-" + nextSuffix(), fixture.productId(), BigDecimal.ONE, LocalDate.now()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void inventoryApiReturnsPaginatedCurrentStockAndProductDetail() throws Exception {
        Fixture fixture = fixture(nextSuffix());
        createPurchase("PO-INV-" + nextSuffix(), fixture.supplierId(), fixture.productId(),
                BigDecimal.valueOf(12), LocalDate.now());

        mockMvc.perform(get("/api/inventory")
                        .param("keyword", fixture.productCode())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(fixture.productId()))
                .andExpect(jsonPath("$.content[0].currentStock").value(12.000));

        mockMvc.perform(get("/api/inventory/products/{productId}", fixture.productId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value(fixture.productCode()));

        mockMvc.perform(get("/api/inventory").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void slowMovingReportIncludesPositiveStockWithoutRecentSalesAndValidatesDays() throws Exception {
        String suffix = nextSuffix();
        Fixture slow = fixture("SLOW-" + suffix);
        createPurchase("PO-SLOW-" + suffix, slow.supplierId(), slow.productId(),
                BigDecimal.valueOf(20), LocalDate.now().minusDays(40));

        Fixture recent = fixture("RECENT-" + suffix);
        createPurchase("PO-RECENT-" + suffix, recent.supplierId(), recent.productId(),
                BigDecimal.valueOf(20), LocalDate.now().minusDays(40));
        createSale("SO-RECENT-" + suffix, recent.productId(), BigDecimal.valueOf(2), LocalDate.now());

        mockMvc.perform(get("/api/reports/slow-moving-products")
                        .param("days", "30")
                        .param("keyword", slow.productCode())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lastSalesDate").doesNotExist());

        mockMvc.perform(get("/api/reports/slow-moving-products")
                        .param("days", "30")
                        .param("keyword", recent.productCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/reports/slow-moving-products").param("days", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private Fixture fixture(String suffix) throws Exception {
        Long supplierId = createSupplier("Fixture Supplier " + suffix, Status.ACTIVE);
        Long categoryId = createCategory("Fixture Category " + suffix, Status.ACTIVE);
        String productCode = "PROD-" + suffix;
        Long productId = createProduct(categoryId, productCode, "Product " + suffix, Status.ACTIVE);
        return new Fixture(supplierId, categoryId, productId, productCode);
    }

    private Long createSupplier(String name, Status status) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "status", status.name(), "contactPhone", "13800000000"))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private Long createCategory(String name, Status status) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "description", "test", "status", status.name()))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private Long createProduct(Long categoryId, String code, String name, Status status) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(productPayload(categoryId, code, name, status))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private Long createPurchase(String orderNo, Long supplierId, Long productId, BigDecimal quantity, LocalDate inboundDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(purchasePayload(orderNo, supplierId, productId, quantity, inboundDate))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private Long createSale(String orderNo, Long productId, BigDecimal quantity, LocalDate outboundDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(salesPayload(orderNo, productId, quantity, outboundDate))))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private Map<String, Object> productPayload(Long categoryId, String code, String name, Status status) {
        return Map.of(
                "categoryId", categoryId,
                "code", code,
                "name", name,
                "unit", "kg",
                "shelfLifeDays", 7,
                "suggestedSalePrice", "12.50",
                "status", status.name());
    }

    private Map<String, Object> purchasePayload(String orderNo, Long supplierId, Long productId,
                                                BigDecimal quantity, LocalDate inboundDate) {
        return Map.of(
                "orderNo", orderNo,
                "supplierId", supplierId,
                "inboundDate", inboundDate.toString(),
                "remark", "test purchase",
                "items", List.of(Map.of(
                        "productId", productId,
                        "quantity", quantity,
                        "unitCost", "5.20")));
    }

    private Map<String, Object> salesPayload(String orderNo, Long productId, BigDecimal quantity, LocalDate outboundDate) {
        return Map.of(
                "orderNo", orderNo,
                "customerName", "Retail Customer",
                "outboundDate", outboundDate.toString(),
                "remark", "test sale",
                "items", List.of(Map.of(
                        "productId", productId,
                        "quantity", quantity,
                        "unitPrice", "8.50")));
    }

    private void assertCurrentStock(Long productId, String expected) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/products/{productId}", productId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("currentStock").decimalValue()).isEqualByComparingTo(new BigDecimal(expected));
    }

    private Long readId(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String nextSuffix() {
        return String.format("%04d", SEQUENCE.incrementAndGet());
    }

    private record Fixture(Long supplierId, Long categoryId, Long productId, String productCode) {
    }
}
