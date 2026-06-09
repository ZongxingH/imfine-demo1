# Project Overview

Status: updated from run evidence on 2026-06-09.

This workspace now contains a fresh food purchase-sales-inventory management system with a Spring Boot backend and a Vite React TypeScript PC admin console.

## Implemented Scope

- Supplier archive management: create, edit, detail, paginated search, validation, and duplicate-name conflict handling.
- Product category and product archive management: create, edit, detail, paginated search, validation, active-category/product rules, and duplicate product-code handling.
- Purchase inbound documents: create, edit, detail, paginated search, server-calculated totals, stock movement creation/replacement, duplicate order rejection, inactive supplier/product rejection, and projected negative-stock rejection on edits.
- Sales outbound documents: create, edit, detail, paginated search, server-calculated totals, stock movement creation/replacement, duplicate order rejection, inactive product rejection, and insufficient-stock rejection.
- Inventory and reports: current-stock query, product stock detail, movement ledger, and slow-moving product report.
- PC admin UI: pages for suppliers, categories, products, purchases, sales, inventory, and slow-moving report.

## Primary Evidence

- Design: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/design/architecture.md`
- API/data model: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/design/api-and-data-model.md`
- Acceptance: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/acceptance-matrix.json`
- QA revalidation: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/agents/qa-revalidation/handoff.json`
- Reviewer revalidation: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/agents/reviewer-revalidation/handoff.json`
- Backend source: `backend/src/main/java/com/imfine/freshinventory/**`
- Frontend source: `frontend/src/**`

## Current Verification Status

Final acceptance matrix reports `overall_status: pass` and `required_coverage_declared_complete: true`. Backend `mvn test` passes only when rerun with escalation in this environment because sandboxed Mockito/ByteBuddy JVM self-attach fails before assertions. Frontend `npm run build` passes in the default sandbox.
