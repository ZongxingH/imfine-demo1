# Module Map

Status: updated from source evidence on 2026-06-09.

## Backend Modules

- Application entry: `backend/src/main/java/com/imfine/freshinventory/FreshInventoryApplication.java`
- Supplier archive:
  - Controller: `backend/src/main/java/com/imfine/freshinventory/controller/SupplierController.java`
  - Service: `backend/src/main/java/com/imfine/freshinventory/service/SupplierService.java`
  - Domain/repository/DTO: `Supplier.java`, `SupplierRepository.java`, `SupplierDtos.java`
- Category archive:
  - Controller/service: `CategoryController.java`, `CategoryService.java`
  - Domain/repository/DTO: `ProductCategory.java`, `ProductCategoryRepository.java`, `CategoryDtos.java`
- Product archive:
  - Controller/service: `ProductController.java`, `ProductService.java`
  - Domain/repository/DTO: `Product.java`, `ProductRepository.java`, `ProductDtos.java`
- Purchase inbound:
  - Controller/service: `PurchaseController.java`, `PurchaseService.java`
  - Domain/repository/DTO: `PurchaseOrder.java`, `PurchaseOrderItem.java`, `PurchaseOrderRepository.java`, `DocumentDtos.java`
- Sales outbound:
  - Controller/service: `SalesController.java`, `SalesService.java`
  - Domain/repository/DTO: `SalesOrder.java`, `SalesOrderItem.java`, `SalesOrderRepository.java`, `DocumentDtos.java`
- Inventory/reporting:
  - Controllers: `InventoryController.java`, `ReportController.java`
  - Service: `InventoryService.java`
  - Domain/repository/DTO: `StockMovement.java`, `StockMovementRepository.java`, `InventoryDtos.java`
- Shared backend:
  - Exceptions: `exception/ApiException.java`, `exception/ErrorCode.java`, `exception/GlobalExceptionHandler.java`, `exception/ApiErrorResponse.java`
  - Validation: `validation/ValidationUtils.java`, `validation/PageRequestFactory.java`

All backend paths above are under `backend/src/main/java/com/imfine/freshinventory/`.

## Frontend Modules

- Shell/navigation: `frontend/src/App.tsx`
- Pages: `frontend/src/pages/SuppliersPage.tsx`, `CategoriesPage.tsx`, `ProductsPage.tsx`, `PurchasesPage.tsx`, `SalesPage.tsx`, `InventoryPage.tsx`, `SlowMovingReportPage.tsx`
- API: `frontend/src/api/http.ts`, `frontend/src/api/resources.ts`
- Types: `frontend/src/types/index.ts`
- Validation: `frontend/src/validation/validators.ts`
- Shared components: `frontend/src/components/**`
- Styling: `frontend/src/styles.css`

## Tests

- Backend integration/API tests: `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java`
- No frontend unit/E2E test suite is present; frontend verification is source inspection plus `npm run build`.
