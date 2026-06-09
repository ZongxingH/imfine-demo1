# Test Strategy

Status: updated from QA evidence on 2026-06-09.

## Backend

Backend verification uses `mvn test` from `backend/`.

The current test suite is `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java`, a Spring Boot API/integration suite covering:

- Supplier archive create/detail/edit/page/validation/conflict.
- Category and product create/detail/edit/page/business errors.
- Purchase create/detail/edit/page, inventory movement replacement, negative projected stock rejection, duplicate order number, inactive supplier, inactive product, and validation.
- Sales create/detail/edit/page, inventory movement replacement, insufficient stock rejection, duplicate order number, inactive product, and validation.
- Current inventory list/detail/movement ledger.
- Slow-moving report and `days` validation.
- Representative global error response behavior.

Final QA reports 12 tests passing with escalation: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`.

## Frontend

Frontend verification uses `npm run build` from `frontend/`, running TypeScript project build and Vite production build.

No frontend unit or browser E2E test suite is present. QA and reviewer evidence treat frontend verification as source inspection plus production build.

## Known Verification Constraint

Default sandbox backend test execution fails before assertions because Mockito/ByteBuddy cannot self-attach to the JVM. This is classified as an environment constraint because escalated `mvn test` passes.

Evidence: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/test-results.md`, `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/acceptance-matrix.json`.
