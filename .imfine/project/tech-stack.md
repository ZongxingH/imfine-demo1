# Tech Stack

Status: updated from source evidence on 2026-06-09.

## Backend

- Java 17.
- Spring Boot 3.3.5.
- Spring Web.
- Spring Validation / Jakarta Bean Validation.
- Spring Data JPA.
- H2 runtime database.
- Maven build via `backend/pom.xml`.
- JUnit/Spring Boot test stack via `spring-boot-starter-test`.

Evidence: `backend/pom.xml`, `backend/src/main/resources/application.yml`.

## Frontend

- React 19.
- React DOM 19.
- TypeScript 5.8.
- Vite 7.
- `@vitejs/plugin-react`.
- npm lockfile for reproducible frontend installs.

Evidence: `frontend/package.json`, `frontend/package-lock.json`, `frontend/vite.config.ts`.

## Build And Verification Commands

- Backend: `mvn test` from `backend/`. In this environment it requires escalated execution because default sandbox JVM self-attach fails before assertions.
- Frontend: `npm run build` from `frontend/`, which runs `tsc -b && vite build`.

Evidence: `.imfine/runs/20260609-搭建生鲜进销存管理系统-包含供应商档案管理-商品品类与商品档案维护-采购入库单据-销售开单出库-/evidence/test-results.md`.
