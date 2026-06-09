# Risks

Status: updated from QA/reviewer evidence on 2026-06-09.

## Active Residual Risks

- Sandboxed backend tests fail before assertions because Mockito/ByteBuddy cannot self-attach to the JVM. Mitigation: run `mvn test` from `backend/` with approved escalation in this environment; final QA confirms that path passes.
- No frontend unit or browser E2E tests are present. Mitigation: current acceptance relies on source inspection plus `npm run build`.
- Backend API tests are broad integration tests but do not exhaustively cover every optional filter and date-range branch.

## Remediated Risks From Review

- Purchase edit negative-stock risk: remediated. `PurchaseService.update` validates projected stock before movement replacement, and the backend test suite covers purchase reduction after dependent sales.
- Frontend build reproducibility risk: remediated. `frontend/package.json` declares the React/Vite/TypeScript toolchain, `frontend/package-lock.json` exists, and `npm run build` passes.
- REST coverage gaps for detail/edit/conflict/inactive product paths: remediated for the previously identified gaps.

Evidence: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/review.md`, `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/agents/reviewer-revalidation/handoff.json`, `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/agents/qa-revalidation/handoff.json`.
