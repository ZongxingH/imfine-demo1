# Reviewer Revalidation Evidence

Run: `20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-`

Approval status: `approved_with_risks`

## Final Findings

No blocking findings remain. The three prior review findings have been remediated and verified. I did not identify any newly introduced serious issues in the reviewed remediation scope.

Residual risk: sandboxed backend test execution still fails because Mockito/Byte Buddy cannot self-attach to the local JVM in the sandbox. The same `mvn test` command passed when rerun with approved escalation, so this is treated as an environment/runtime constraint rather than an implementation failure.

## Remediation Review

### Prior High: Purchase edits can drive inventory negative after dependent sales

Status: remediated.

Evidence:
- `backend/src/main/java/com/imfine/freshinventory/service/PurchaseService.java:55` validates projected stock before old purchase movements are removed.
- `backend/src/main/java/com/imfine/freshinventory/service/PurchaseService.java:114` aggregates old and replacement purchase quantities by product.
- `backend/src/main/java/com/imfine/freshinventory/service/PurchaseService.java:128` computes `currentStock - oldQuantity + newQuantity`.
- `backend/src/main/java/com/imfine/freshinventory/service/PurchaseService.java:129` rejects projected negative stock with `INSUFFICIENT_STOCK`.
- `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:168` covers the prior failure mode: purchase 100, sell 80, attempt to edit purchase down to 50, expect rejection, and verify stock remains 20.

### Prior Medium: Frontend build is not reproducible/verifiable

Status: remediated.

Evidence:
- `frontend/package.json:8` defines the build as `tsc -b && vite build`.
- `frontend/package.json:11` declares React runtime dependencies.
- `frontend/package.json:15` declares React type packages, Vite React plugin, TypeScript, and Vite as dev dependencies.
- `frontend/package-lock.json:1` exists and records the root dependency/devDependency set for reproducible installs.
- `npm run build` from `frontend/` passed.

### Prior Medium: REST API coverage missing detail/edit/conflict/inactive-product paths

Status: remediated for the previously identified gaps.

Evidence:
- Category detail is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:88`.
- Product edit is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:116`.
- Purchase detail is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:147`.
- Sales detail is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:248`.
- Duplicate purchase order number rejection is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:187`.
- Duplicate sales order number rejection is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:268`.
- Inactive product rejection for purchase is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:216`.
- Inactive product rejection for sales is asserted at `backend/src/test/java/com/imfine/freshinventory/FreshInventoryApiTest.java:295`.

## Verification

- `npm run build` from `frontend/`: passed. TypeScript project build and Vite production build completed; Vite transformed 45 modules and emitted `dist` assets.
- `mvn test` from `backend/` under sandbox: failed before test assertions with Mockito/Byte Buddy JVM self-attach initialization errors; 12 tests reported as errors, 0 assertion failures.
- `mvn test` from `backend/` with approved escalation: passed. Tests run: 12, Failures: 0, Errors: 0, Skipped: 0; BUILD SUCCESS.

## Scope Notes

- I did not modify backend, frontend, QA evidence, final gates, or `orchestration/orchestrator-session.json`.
- Files modified by this revalidation: this review evidence file and `agents/reviewer-revalidation/handoff.json`.
