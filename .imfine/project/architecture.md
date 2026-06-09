# Architecture

Status: updated from run evidence on 2026-06-09.

## Backend

Backend is a single Spring Boot application under `backend/`, using layered packages:

- `controller/`: REST endpoints under `/api`, request validation, pagination parameters.
- `service/`: business rules and transaction boundaries.
- `repository/`: Spring Data JPA repositories and query methods.
- `domain/`: JPA entities and enums.
- `dto/`: request/response/page DTOs.
- `exception/`: shared API exception shape and global exception handling.
- `validation/`: shared validation guards and page request helper.

Source evidence: `backend/src/main/java/com/imfine/freshinventory/**`.

## Inventory Model

Inventory is derived from immutable-style `stock_movement` rows rather than a mutable stock balance table:

- Purchase inbound writes positive `PURCHASE_IN` movements.
- Sales outbound writes negative `SALES_OUT` movements.
- Current stock is aggregated from movement deltas.
- Purchase and sales edits replace source movements transactionally.
- Sales creation/editing rejects insufficient stock.
- Purchase edits validate projected stock before replacing movements so dependent sales cannot leave negative inventory.

Evidence: `backend/src/main/java/com/imfine/freshinventory/service/InventoryService.java`, `backend/src/main/java/com/imfine/freshinventory/service/PurchaseService.java`, `backend/src/main/java/com/imfine/freshinventory/service/SalesService.java`, and reviewer revalidation evidence.

## Frontend

Frontend is a Vite React TypeScript application under `frontend/`.

- `src/App.tsx`: admin navigation and route selection.
- `src/pages/`: module pages.
- `src/api/`: shared HTTP and typed API resources.
- `src/components/`: table, modal, pagination, forms, status badge, and page header components.
- `src/types/`: DTO shapes matching backend contracts.
- `src/validation/`: reusable client-side validators.

Vite proxies `/api` to `http://localhost:8080` during development.

## Cross-Cutting Contracts

- REST base path: `/api`.
- Page response shape: `content`, `page`, `size`, `totalElements`, `totalPages`.
- Error response shape: `code`, `message`, `details`, `timestamp`.
- Business errors use `ApiException` and `ErrorCode`; validation errors flow through `GlobalExceptionHandler`.

Evidence: design documents, `frontend/src/api/http.ts`, `backend/src/main/java/com/imfine/freshinventory/exception/GlobalExceptionHandler.java`, and `backend/src/main/java/com/imfine/freshinventory/dto/CommonDtos.java`.
