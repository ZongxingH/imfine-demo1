# API And Data Model Design

## API Conventions

- Base path: `/api`
- Content type: JSON
- Pagination parameters: `page`, `size`, optional sort handled by endpoint-specific query parameters.
- Common page response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

- Common error response:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [],
  "timestamp": "2026-06-09T00:00:00Z"
}
```

## Core Tables

### supplier

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| name | varchar(120) | required, unique |
| contact_name | varchar(80) | optional |
| contact_phone | varchar(40) | optional |
| address | varchar(255) | optional |
| status | varchar(20) | ACTIVE, INACTIVE |
| created_at | timestamp | required |
| updated_at | timestamp | required |

### product_category

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| name | varchar(120) | required, unique |
| description | varchar(255) | optional |
| status | varchar(20) | ACTIVE, INACTIVE |
| created_at | timestamp | required |
| updated_at | timestamp | required |

### product

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| category_id | bigint | FK to product_category |
| code | varchar(60) | required, unique |
| name | varchar(160) | required |
| unit | varchar(20) | required |
| shelf_life_days | int | optional positive value |
| suggested_sale_price | decimal(18,2) | optional non-negative value |
| status | varchar(20) | ACTIVE, INACTIVE |
| created_at | timestamp | required |
| updated_at | timestamp | required |

### purchase_order

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| order_no | varchar(60) | required, unique |
| supplier_id | bigint | FK to supplier |
| inbound_date | date | required |
| total_amount | decimal(18,2) | derived from items |
| remark | varchar(255) | optional |
| created_at | timestamp | required |
| updated_at | timestamp | required |

### purchase_order_item

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| purchase_order_id | bigint | FK to purchase_order |
| product_id | bigint | FK to product |
| quantity | decimal(18,3) | required positive value |
| unit_cost | decimal(18,2) | required non-negative value |
| amount | decimal(18,2) | quantity * unit_cost |

### sales_order

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| order_no | varchar(60) | required, unique |
| customer_name | varchar(120) | optional |
| outbound_date | date | required |
| total_amount | decimal(18,2) | derived from items |
| remark | varchar(255) | optional |
| created_at | timestamp | required |
| updated_at | timestamp | required |

### sales_order_item

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| sales_order_id | bigint | FK to sales_order |
| product_id | bigint | FK to product |
| quantity | decimal(18,3) | required positive value |
| unit_price | decimal(18,2) | required non-negative value |
| amount | decimal(18,2) | quantity * unit_price |

### stock_movement

| Column | Type | Notes |
| --- | --- | --- |
| id | bigint identity | primary key |
| product_id | bigint | FK to product |
| movement_type | varchar(20) | PURCHASE_IN, SALES_OUT |
| source_type | varchar(30) | PURCHASE_ORDER, SALES_ORDER |
| source_id | bigint | source document id |
| source_item_id | bigint | source document item id |
| quantity_delta | decimal(18,3) | positive for inbound, negative for outbound |
| movement_date | date | document business date |
| created_at | timestamp | required |

Indexes:

- `supplier(name)`
- `product_category(name)`
- `product(code)`
- `purchase_order(order_no)`
- `sales_order(order_no)`
- `stock_movement(product_id, movement_date)`
- `stock_movement(source_type, source_id)`

## REST Endpoints

### Suppliers

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/suppliers` | paginated search by `keyword`, `status` |
| GET | `/api/suppliers/{id}` | detail |
| POST | `/api/suppliers` | create |
| PUT | `/api/suppliers/{id}` | edit |

### Product Categories

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/categories` | paginated search by `keyword`, `status` |
| GET | `/api/categories/{id}` | detail |
| POST | `/api/categories` | create |
| PUT | `/api/categories/{id}` | edit |

### Products

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/products` | paginated search by `keyword`, `categoryId`, `status` |
| GET | `/api/products/{id}` | detail |
| POST | `/api/products` | create |
| PUT | `/api/products/{id}` | edit |

### Purchase Inbound

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/purchases` | paginated search by `orderNo`, `supplierId`, date range |
| GET | `/api/purchases/{id}` | detail with items |
| POST | `/api/purchases` | create purchase inbound and stock movements |
| PUT | `/api/purchases/{id}` | edit purchase document and reconcile movements if document editing is supported |

If editing purchase documents is implemented, the service must update document items and replace related stock movements in one transaction.

### Sales Outbound

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/sales` | paginated search by `orderNo`, `customerName`, date range |
| GET | `/api/sales/{id}` | detail with items |
| POST | `/api/sales` | create sales outbound and stock movements |
| PUT | `/api/sales/{id}` | edit sales document and reconcile movements if document editing is supported |

Sales creation and editing must reject insufficient stock with `INSUFFICIENT_STOCK`.

### Inventory

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/inventory` | paginated current stock by product/category/keyword |
| GET | `/api/inventory/products/{productId}` | current stock detail for one product |
| GET | `/api/inventory/products/{productId}/movements` | paginated movement ledger |

Current stock is calculated from `stock_movement.quantity_delta`.

### Reports

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/reports/slow-moving-products` | slow-moving product report |

Query parameters:

- `days`: default 30; products without sales movement in this many days are slow-moving.
- `categoryId`: optional.
- `keyword`: optional product code/name search.
- `page`, `size`: pagination.

Report row fields:

- product id, code, name, category name, unit
- current stock
- last sales date
- days since last sale

Products with positive stock and no sales history should be included with `lastSalesDate = null`.

## Request DTO Sketches

### Create Or Update Supplier

```json
{
  "name": "Fresh Supplier",
  "contactName": "Contact",
  "contactPhone": "13800000000",
  "address": "Address",
  "status": "ACTIVE"
}
```

### Create Or Update Product

```json
{
  "categoryId": 1,
  "code": "APPLE-001",
  "name": "Apple",
  "unit": "kg",
  "shelfLifeDays": 7,
  "suggestedSalePrice": 12.50,
  "status": "ACTIVE"
}
```

### Create Purchase

```json
{
  "orderNo": "PO202606090001",
  "supplierId": 1,
  "inboundDate": "2026-06-09",
  "remark": "Morning inbound",
  "items": [
    {
      "productId": 1,
      "quantity": 100.000,
      "unitCost": 5.20
    }
  ]
}
```

### Create Sale

```json
{
  "orderNo": "SO202606090001",
  "customerName": "Retail Customer",
  "outboundDate": "2026-06-09",
  "remark": "Counter sale",
  "items": [
    {
      "productId": 1,
      "quantity": 10.000,
      "unitPrice": 8.50
    }
  ]
}
```

## Business Rule Notes

- Master data referenced by documents should not be hard-deleted by this scope.
- Inactive suppliers cannot be used for new purchase inbound documents.
- Inactive categories cannot be assigned to new active products.
- Inactive products cannot be used in new purchase or sales documents.
- Duplicate document numbers must be rejected.
- Document totals should be calculated on the backend from items.
- Stock checks and stock movement writes must happen in the same service transaction.
