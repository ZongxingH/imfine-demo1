# Product

Status: updated from run evidence on 2026-06-09.

The product is a PC-admin fresh goods inventory system for basic archives, purchase inbound, sales outbound, real-time stock, and slow-moving inventory analysis.

## User-Facing Capabilities

- Basic archives: suppliers, product categories, and products.
- Business documents: purchase inbound orders and sales outbound orders.
- Inventory operations: current stock by product/category, product movement ledger, and stock effects from purchase/sales documents.
- Reporting: slow-moving products with positive stock and no recent sales according to a `days` threshold.

## UX Surface

The admin console is implemented as an operational workspace, not a landing page. `frontend/src/App.tsx` routes users to:

- `/suppliers`
- `/categories`
- `/products`
- `/purchases`
- `/sales`
- `/inventory`
- `/reports/slow-moving`

Pages provide table views, pagination, filters, add/edit forms, loading/error states, and form validation entry points. Typed API helpers live in `frontend/src/api/resources.ts`; reusable validation helpers live in `frontend/src/validation/validators.ts`.

## Out Of Scope For This Run

Authentication, authorization, audit trails, multi-tenancy, external databases, cache, queue, and browser E2E tests were not implemented in this run. These were not required by the final acceptance matrix.
