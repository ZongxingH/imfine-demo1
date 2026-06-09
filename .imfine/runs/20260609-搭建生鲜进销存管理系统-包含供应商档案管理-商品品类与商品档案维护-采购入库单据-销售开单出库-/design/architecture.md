# Architecture Design

## Scope

Build a fresh food purchase-sales-inventory management system with:

- supplier master data
- product category and product master data
- purchase inbound documents
- sales outbound documents
- real-time stock calculation
- slow-moving product statistics
- PC admin console with create, edit, paginated search, and form validation
- unit tests for all business REST interfaces

This is a new empty project. No existing application source or preset common utility code was found. The project should therefore define the shared backend exception and validation utilities once, then require controllers and services to reuse them.

## Assumptions

- Backend: single Maven Spring Boot application in `backend/`.
- Backend persistence: H2 for local development and tests.
- Backend architecture: layered packages for controller, service, repository, domain, dto, exception, and validation.
- Frontend: Vite React TypeScript application in `frontend/`.
- Authentication, authorization, audit trails, and multi-tenant separation are out of scope unless later requirements add them.
- Monetary and quantity values use decimal types, not floating point.

## Repository Layout

```text
backend/
  pom.xml
  src/main/java/.../
    FreshInventoryApplication.java
    controller/
    service/
    repository/
    domain/
    dto/
    exception/
    validation/
  src/main/resources/
    application.yml
    db/migration/ or schema.sql
  src/test/java/.../
frontend/
  package.json
  src/
    api/
    pages/
    components/
    routes/
    types/
    validation/
```

## Backend Layers

### Controller Layer

- Owns REST routing, request DTO validation, pagination parameters, and response DTO mapping.
- Uses Jakarta Bean Validation annotations on request DTOs.
- Does not contain business rules beyond request boundary checks.
- Delegates failures to global exception handling.

### Service Layer

- Owns business rules and transaction boundaries.
- Validates cross-record constraints, such as active suppliers, active products, duplicate document numbers, and stock sufficiency for sales outbound.
- Creates immutable stock movement records for inbound and outbound documents.
- Does not directly return persistence entities to controllers.

### Repository Layer

- Uses Spring Data JPA repositories for CRUD and query methods.
- Provides custom query methods only where pagination, slow-moving reports, or stock aggregation need them.
- Keeps SQL/JPA query complexity close to data access and away from controllers.

### Domain Layer

- Contains JPA entities for supplier, product category, product, purchase order, purchase order item, sales order, sales order item, and stock movement.
- Uses status fields for soft business state where records may be referenced by documents.
- Does not expose web validation concerns.

### DTO Layer

- Request DTOs: create/update/search payloads with Bean Validation annotations.
- Response DTOs: stable API shape for lists, details, and report rows.
- Shared page response DTO for paginated endpoints.

### Exception And Validation Utilities

Because no preset common utilities exist, define these once in `backend/src/main/java/.../exception` and `validation`:

- `ApiException`: business exception carrying an error code and message.
- `ErrorCode`: enum for validation, not found, conflict, insufficient stock, and internal errors.
- `GlobalExceptionHandler`: maps validation and business exceptions to consistent JSON responses.
- `ApiErrorResponse`: `{ code, message, details, timestamp }`.
- `ValidationUtils`: reusable guards such as `requireFound`, `requireTrue`, `requirePositive`, and `requireUnique`.

Controllers and services must reuse these utilities rather than creating endpoint-specific exception formats or ad hoc guard methods.

## Inventory Model

Inventory must be computed from `stock_movement` rows:

- purchase inbound creates positive movement rows.
- sales outbound creates negative movement rows.
- current stock equals `sum(quantity_delta)` grouped by product.
- sales outbound must check available stock inside a transaction before persisting movements.

This avoids silent drift between document rows and stock balances. If a cached stock balance table is later needed for performance, it should be treated as a derived projection and reconciled from movements.

## Frontend Architecture

The PC admin console should be a practical CRUD workspace, not a landing page.

Recommended routes:

- `/suppliers`
- `/categories`
- `/products`
- `/purchases`
- `/sales`
- `/inventory`
- `/reports/slow-moving`

Frontend modules:

- `api/`: typed API clients and shared request helpers.
- `types/`: DTO types matching backend API responses.
- `validation/`: form schemas or reusable field validators.
- `pages/`: route-level screens.
- `components/`: shared table, pagination, form field, modal/drawer, and status controls.

Each master-data page should support paginated search, create, and edit. Document pages should support create, edit before finalization if implemented, detail view, and paginated search. Inventory and slow-moving report pages are read-only query/report views.

## Validation Rules

Backend is the source of truth for validation. Frontend validation improves user experience but must mirror backend rules.

Minimum backend validation:

- supplier name: required, unique, max length.
- contact phone: optional but format checked when present.
- category name: required, unique.
- product code: required, unique.
- product name: required.
- unit: required.
- shelf life days: positive when present.
- purchase quantity and sales quantity: positive decimal.
- purchase unit cost and sales unit price: non-negative decimal.
- purchase/sales document items: at least one item.
- sales outbound: reject quantities exceeding current stock.

## Testing Strategy

All business REST interfaces require unit or slice tests:

- controller tests for validation failures, success responses, and pagination parameter handling.
- service tests for business rules, duplicate checks, stock movement creation, and insufficient stock rejection.
- repository/query tests for stock aggregation and slow-moving report behavior using H2.

Suggested commands after implementation:

- backend: `mvn test` from `backend/`
- frontend: `npm test` or `npm run test` if the frontend test stack is added by Dev

## Non-Goals

- Do not implement application source in architecture handoff artifacts.
- Do not modify orchestration runtime files.
- Do not introduce external database, cache, queue, or authentication dependencies unless a later requirement explicitly adds them.
