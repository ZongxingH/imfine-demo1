# Conventions

Status: updated from source evidence on 2026-06-09.

## Backend

- Keep REST controllers thin; business rules belong in services.
- Use `@Transactional` on service methods for write flows and `@Transactional(readOnly = true)` for reads.
- Use DTO records/classes for request and response shapes; do not return JPA entities directly from controllers.
- Use `ValidationUtils` and `ApiException`/`ErrorCode` for service-layer business errors.
- Use Jakarta Bean Validation and `GlobalExceptionHandler` for API boundary validation.
- Use `PageRequestFactory` and shared page DTOs for pagination.
- Use `BigDecimal` for quantity and money values.
- Treat `stock_movement` rows as the inventory source of truth.

Evidence: `backend/src/main/java/com/imfine/freshinventory/controller/**`, `service/**`, `dto/**`, `exception/**`, `validation/**`.

## Frontend

- Use typed API helpers in `frontend/src/api/resources.ts` rather than hard-coding fetch calls in each page.
- Keep DTO types in `frontend/src/types/index.ts`.
- Keep reusable validators in `frontend/src/validation/validators.ts`.
- Pages manage their module-specific filters, forms, modals, and validation.
- Shared components live in `frontend/src/components/**`.

Evidence: `frontend/src/pages/**`, `frontend/src/api/**`, `frontend/src/components/**`.

## imfine Run Boundaries

The run evidence repeatedly enforces role-owned writes and no changes to `orchestration/orchestrator-session.json` from non-orchestrator roles. This knowledge update only modifies `.imfine/project/**` and its own handoff.
