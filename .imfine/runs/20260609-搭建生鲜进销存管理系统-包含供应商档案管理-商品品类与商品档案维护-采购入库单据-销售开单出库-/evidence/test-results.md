# QA Revalidation Test Results

Run ID: `20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-`

QA date: 2026-06-09 Asia/Shanghai

## Scope Read

- Requirement: supplier archives, product category and product archives, purchase inbound documents, sales outbound documents, realtime inventory, slow-moving product report, layered backend REST/data model, PC admin pages, add/edit/paginated query/form validation, business API tests, shared global exception and validation utilities, and no modification of preset common utility code.
- Prior evidence read: `agents/dev-backend-remediation/handoff.json`, `agents/dev-frontend-verification/handoff.json`, `evidence/review.md`, previous `acceptance-matrix.json`, and previous QA handoff.
- Current spot checks: backend test method list, frontend route/page/API/validation entry points.

## Backend Verification

### Command 1

- Working directory: `backend/`
- Command: `mvn test`
- Sandbox: default
- Result: blocked by local JVM attach limitation, exit code 1
- Exact result summary:
  - `FreshInventoryApiTest` started under Spring Boot.
  - Mockito/ByteBuddy inline mock maker failed before test assertions.
  - Key error: `Could not self-attach to current VM using external process`.
  - Maven summary: `Tests run: 12, Failures: 0, Errors: 12, Skipped: 0`.
- Classification: environment/sandbox blocker, not a product assertion failure.

### Command 2

- Working directory: `backend/`
- Command: `mvn test`
- Sandbox: escalated, rerun required because Command 1 failed on Mockito/ByteBuddy JVM attach.
- Result: pass, exit code 0
- Exact result summary:
  - `FreshInventoryApiTest` completed.
  - Maven summary: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`.
  - Maven result: `BUILD SUCCESS`.
- Classification: backend verification pass after required environment escalation.

### Backend Remediation Notes

- Reviewer high finding on purchase edit negative inventory is covered by `purchaseEditRejectsReductionThatWouldMakeStockNegativeAfterSales`.
- Reviewer medium finding on missing REST detail/edit and negative path coverage is addressed by the current 12-test suite, including category detail, product edit, purchase detail, sales detail, duplicate purchase/sales order numbers, inactive purchase product, inactive sales product, and inactive supplier.
- Backend still requires escalation in this environment because default sandbox JVM attach remains blocked. The same command passes outside that sandbox.

## Frontend Verification

### Command

- Working directory: `frontend/`
- Command: `npm run build`
- Sandbox: default
- Result: pass, exit code 0
- Exact result summary:
  - Build script ran `tsc -b && vite build`.
  - Vite transformed `45 modules`.
  - Emitted `dist/index.html`, `dist/assets/index-CDBh7AWd.css`, and `dist/assets/index-C5DkVZpN.js`.
  - Vite result: `built in 324ms`.

### Frontend Remediation Notes

- Previous frontend blocker `sh: tsc: command not found` is resolved in the current workspace.
- Current source inspection confirms PC admin routes/pages for suppliers, categories, products, purchases, sales, inventory, and slow-moving report.
- Current source inspection confirms typed API helpers for list/detail/create/update flows and frontend validators used by the add/edit forms.
- No frontend automated tests are present or required by the normalized requirement; verification is source inspection plus TypeScript/Vite production build.

## Final QA Classification

- Backend verification: pass after required escalation.
- Frontend verification: pass in default sandbox.
- Reviewer remediation status: required backend and frontend findings verified as resolved.
- Required coverage declared complete: yes.
- Blocked required items: none.

Residual risk: default sandbox Maven execution is still not usable for this Mockito/ByteBuddy test suite, so future QA runs in this environment should expect the same escalation path unless the test/runtime configuration changes.
