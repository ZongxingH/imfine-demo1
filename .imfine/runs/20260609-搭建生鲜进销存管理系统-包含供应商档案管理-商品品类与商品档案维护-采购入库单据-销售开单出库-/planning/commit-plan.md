# Commit Plan

## Policy

- Commits are prepared only after QA and reviewer evidence are complete or after a remediation cycle resolves blocking findings.
- Push is outside this planning scope and follows runtime policy.
- Committer/writeup roles own final gate and archive evidence.
- Planning artifacts in this directory are task-planner owned and should remain separate from implementation commits if the committer chooses multiple commits.

## Proposed Commit Order

1. `chore: scaffold layered backend foundation`
   - Scope: `T00-architecture-foundation`
   - Expected files: `backend/**`, root Maven helper files if needed.
   - Gate: backend scaffold builds and foundation tests pass.

2. `feat: add supplier archive management`
   - Scope: `T01-supplier-management`
   - Expected files: `backend/**`
   - Gate: supplier CRUD/page/validation API tests pass.

3. `feat: add category and product archives`
   - Scope: `T02-category-product-management`
   - Expected files: `backend/**`
   - Gate: category and product CRUD/page/validation API tests pass.

4. `feat: add realtime inventory accounting`
   - Scope: `T05-realtime-inventory`
   - Expected files: `backend/**`
   - Gate: inventory increase/decrease/query/insufficient-stock tests pass.

5. `feat: add purchase inbound documents`
   - Scope: `T03-purchase-inbound`
   - Expected files: `backend/**`
   - Gate: purchase inbound tests pass and inventory increases correctly.

6. `feat: add sales outbound documents`
   - Scope: `T04-sales-outbound`
   - Expected files: `backend/**`
   - Gate: sales outbound tests pass and insufficient stock is rejected.

7. `feat: add slow-moving product report`
   - Scope: `T06-slow-moving-report`
   - Expected files: `backend/**`
   - Gate: report tests pass for threshold, pagination, and validation behavior.

8. `feat: add PC inventory admin UI`
   - Scope: `T07-pc-admin-ui`
   - Expected files: `frontend/**`
   - Gate: frontend build and available frontend tests or smoke checks pass.

9. `test: complete REST API coverage`
   - Scope: `T08-rest-api-test-completion`
   - Expected files: `backend/**`
   - Gate: complete backend REST API test suite passes.

10. `test: record QA acceptance evidence`
    - Scope: `T09-qa-acceptance`
    - Expected files: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/test-results.md`, `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/acceptance-matrix.json`, QA handoff.
    - Gate: required coverage is declared complete with no blocked required item.

11. `chore: finalize inventory management delivery`
    - Scope: `T10-review-final-gates`
    - Expected files: review evidence, final gates, committer handoff, archive evidence.
    - Gate: reviewer has no blocking findings and final gates pass.

## Merge And Remediation Rules

- If QA or review finds a blocking issue, create a remediation task targeted to the owning implementation role before final gate.
- Do not squash away QA/review evidence unless the committer explicitly records why a single final commit is preferred.
- Before final commit readiness, verify preset common utility files are unchanged from their baseline or absent in this new project.
