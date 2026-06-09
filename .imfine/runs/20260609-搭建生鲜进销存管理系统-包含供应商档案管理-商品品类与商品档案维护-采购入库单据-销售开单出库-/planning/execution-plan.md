# Execution Plan

## Assumptions

- The project is currently empty except for `.imfine/` runtime metadata.
- Backend implementation may write `backend/**` and root Maven helper files if needed: `pom.xml`, `mvnw`, `mvnw.cmd`, and `.mvn/**`.
- Frontend implementation may write `frontend/**`.
- QA owns `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/test-results.md`, `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/acceptance-matrix.json`, and QA handoff.
- Reviewer owns `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/review.md` and reviewer handoff.
- Committer and archive/writeup roles own final gate and archive evidence.
- Preset common utility code must not be modified. Global exception and parameter validation utilities must be reused through normal extension or integration points.

## Success Criteria

1. Supplier archives support add, edit, paginated query, and validation in backend REST APIs and PC admin UI.
2. Product categories and product archives support add, edit, paginated query, and validation in backend REST APIs and PC admin UI.
3. Purchase inbound documents support validated create/edit/page workflows and update realtime inventory.
4. Sales outbound documents support validated create/edit/page workflows, deduct stock, and reject insufficient inventory.
5. Realtime inventory APIs return current stock after inbound and outbound operations.
6. Slow-moving product report returns paginated statistics based on clear threshold criteria.
7. All business REST APIs have unit or slice/integration tests for success and validation or business error paths.
8. PC admin pages include the required modules, forms, table pagination, and form validation.
9. QA evidence and acceptance matrix cover every required capability.
10. Review and final gates verify tests, architecture boundaries, and the ban on modifying preset common utilities.

## Work Sequence

1. Architecture foundation
   - Tasks: `T00-architecture-foundation`
   - Verify: backend scaffold builds, global exception/validation integration is defined, table/API conventions are clear.

2. Independent master data features
   - Tasks: `T01-supplier-management`, `T02-category-product-management`
   - Verify: supplier, category, and product REST APIs pass create/edit/page/validation tests.

3. Inventory core
   - Tasks: `T05-realtime-inventory`
   - Verify: inventory service can increase, decrease, reject insufficient stock, and expose paginated query.

4. Document workflows
   - Tasks: `T03-purchase-inbound`, `T04-sales-outbound`
   - Verify: purchase and sales documents validate line items and update inventory transactionally.

5. Reporting
   - Tasks: `T06-slow-moving-report`
   - Verify: report endpoint returns paginated slow-moving rows and validates threshold parameters.

6. PC admin frontend
   - Tasks: `T07-pc-admin-ui`
   - Verify: all required pages are reachable; add/edit/page/form validation flows are implemented or smoke-tested.

7. REST API test closure
   - Tasks: `T08-rest-api-test-completion`
   - Verify: every business API has positive and negative coverage; backend test command passes.

8. QA, review, finalization
   - Tasks: `T09-qa-acceptance`, `T10-review-final-gates`
   - Verify: QA evidence, acceptance matrix, review evidence, final gates, committer handoff, and archive evidence exist and are internally consistent.

## Parallelization

- After `T00`, `T01` and `T02` can run in parallel if agents coordinate shared pagination/error conventions.
- `T05` can run after `T02` and may proceed while `T01` is finishing, because inventory depends on products rather than suppliers.
- `T03` depends on suppliers and products. `T04` depends on products and realtime inventory.
- Frontend `T07` should start after backend API contracts are stable enough to avoid avoidable churn.

## Verification Commands

- Backend command is unknown until implementation chooses the scaffold. Expected Maven command shape: `./mvnw test` or `mvn test`.
- Frontend command is unknown until implementation chooses the scaffold. Expected command shape: package-manager install/build/test commands from `frontend/package.json`.
- QA must record the exact commands and results in `evidence/test-results.md`.

## Guardrails

- Do not modify `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/orchestration/orchestrator-session.json`.
- Do not edit application source from the task-planner role.
- Do not change preset common utility source files. If required utilities are missing in this empty project, implement feature-owned adapters or handlers outside preset utility paths and document the assumption.
- Keep changes surgical and trace each changed line to the requirement or harness evidence.
