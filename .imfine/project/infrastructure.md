# Infrastructure

Status: updated from source and QA evidence on 2026-06-09.

## Runtime Topology

- Backend service runs on port `8080`.
- Backend uses in-memory H2 configured as `jdbc:h2:mem:fresh_inventory;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`.
- Hibernate DDL mode is `create-drop`.
- H2 console is enabled for local development.
- Frontend Vite dev server proxies `/api` to `http://localhost:8080`.

Evidence: `backend/src/main/resources/application.yml`, `frontend/vite.config.ts`.

## Build Artifacts

- Backend Maven test/build artifacts are generated under `backend/target/**`.
- Frontend production build outputs `frontend/dist/**`.
- Frontend dependency install creates `frontend/node_modules/**`; reproducibility is represented by `frontend/package-lock.json`.

## Environment Notes

The final QA evidence records that default sandbox `mvn test` fails before assertions because Mockito/ByteBuddy cannot self-attach to the JVM. The same command passes with approved escalation. Frontend `npm run build` passes in the default sandbox.

Evidence: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/agents/qa-revalidation/handoff.json`.
