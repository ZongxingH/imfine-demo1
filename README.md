# Fresh Inventory Management

Fresh Inventory Management is a fresh goods purchase-sales-inventory system with a Spring Boot backend and a React/Vite PC admin console.

## Scope

The application covers these modules:

- Supplier archives: paginated supplier search, create, detail, and edit.
- Product categories: paginated category search, create, detail, and edit.
- Product archives: product code/name/unit/category/shelf-life/price/status maintenance.
- Purchase inbound documents: purchase order create, detail, edit, line items, totals, and inbound stock movements.
- Sales outbound documents: sales order create, detail, edit, line items, totals, and outbound stock movements.
- Inventory and reports: realtime product stock, stock movement ledger, and slow-moving product report.

## Backend

- Stack: Java 17, Spring Boot 3.3.5, Spring Web, Spring Validation, Spring Data JPA, H2.
- Entry point: `backend/src/main/java/com/imfine/freshinventory/FreshInventoryApplication.java`.
- API base path: `/api`.
- Local server port: `8080`.
- Local database: in-memory H2, configured as `jdbc:h2:mem:fresh_inventory;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`.

Main API groups:

- `/api/suppliers`
- `/api/categories`
- `/api/products`
- `/api/purchases`
- `/api/sales`
- `/api/inventory`
- `/api/reports/slow-moving-products`

Data and inventory model:

- `Supplier`, `ProductCategory`, and `Product` hold master data with `ACTIVE` or `INACTIVE` status.
- `PurchaseOrder` and `PurchaseOrderItem` record supplier inbound documents, item quantities, unit costs, and totals.
- `SalesOrder` and `SalesOrderItem` record outbound sales documents, customers, item quantities, unit prices, and totals.
- `StockMovement` is the inventory ledger. Purchases create positive movement deltas and sales create negative movement deltas.
- Current stock is derived from summed movement deltas per product. Sales and purchase edits validate projected stock so inventory cannot go negative.

## Frontend

- Stack: React 19, TypeScript 5.8, Vite 7, `@vitejs/plugin-react`.
- API client base: `/api`.
- Vite dev proxy: `/api` forwards to `http://localhost:8080`.

Admin views:

- `/suppliers`: supplier archives.
- `/categories`: product categories.
- `/products`: product archives.
- `/purchases`: purchase inbound documents.
- `/sales`: sales outbound documents.
- `/inventory`: realtime inventory and movement ledger.
- `/reports/slow-moving`: slow-moving product report.

## Setup And Run

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

For a production frontend build:

```bash
cd frontend
npm run build
```

## Verification

Backend integration tests:

```bash
cd backend
mvn test
```

Frontend TypeScript and production build:

```bash
cd frontend
npm run build
```

Known local caveat: in this workspace, sandboxed Maven can fail before assertions because Mockito/ByteBuddy cannot self-attach to the JVM. QA reran the same backend command with approved escalation, and the normal Maven run passed with `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`. The frontend build passed in the default sandbox.
