import { requestJson } from "./http";
import type {
  CategoryPayload,
  InventoryRow,
  PageResponse,
  Product,
  ProductCategory,
  ProductPayload,
  PurchaseOrder,
  PurchasePayload,
  SalesOrder,
  SalesPayload,
  SlowMovingRow,
  Status,
  StockMovement,
  Supplier,
  SupplierPayload
} from "../types";

interface PageQuery {
  page: number;
  size: number;
}

export interface StatusQuery extends PageQuery {
  keyword?: string;
  status?: Status | "";
}

export interface ProductQuery extends StatusQuery {
  categoryId?: number | "";
}

export interface PurchaseQuery extends PageQuery {
  orderNo?: string;
  supplierId?: number | "";
  fromDate?: string;
  toDate?: string;
}

export interface SalesQuery extends PageQuery {
  orderNo?: string;
  customerName?: string;
  fromDate?: string;
  toDate?: string;
}

export interface InventoryQuery extends PageQuery {
  keyword?: string;
  categoryId?: number | "";
}

export interface SlowMovingQuery extends InventoryQuery {
  days: number;
}

export const suppliersApi = {
  list: (query: StatusQuery) => requestJson<PageResponse<Supplier>>("/suppliers", {}, query),
  detail: (id: number) => requestJson<Supplier>(`/suppliers/${id}`),
  create: (payload: SupplierPayload) =>
    requestJson<Supplier>("/suppliers", { method: "POST", body: JSON.stringify(payload) }),
  update: (id: number, payload: SupplierPayload) =>
    requestJson<Supplier>(`/suppliers/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const categoriesApi = {
  list: (query: StatusQuery) => requestJson<PageResponse<ProductCategory>>("/categories", {}, query),
  detail: (id: number) => requestJson<ProductCategory>(`/categories/${id}`),
  create: (payload: CategoryPayload) =>
    requestJson<ProductCategory>("/categories", { method: "POST", body: JSON.stringify(payload) }),
  update: (id: number, payload: CategoryPayload) =>
    requestJson<ProductCategory>(`/categories/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const productsApi = {
  list: (query: ProductQuery) => requestJson<PageResponse<Product>>("/products", {}, query),
  detail: (id: number) => requestJson<Product>(`/products/${id}`),
  create: (payload: ProductPayload) =>
    requestJson<Product>("/products", { method: "POST", body: JSON.stringify(payload) }),
  update: (id: number, payload: ProductPayload) =>
    requestJson<Product>(`/products/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const purchasesApi = {
  list: (query: PurchaseQuery) => requestJson<PageResponse<PurchaseOrder>>("/purchases", {}, query),
  detail: (id: number) => requestJson<PurchaseOrder>(`/purchases/${id}`),
  create: (payload: PurchasePayload) =>
    requestJson<PurchaseOrder>("/purchases", { method: "POST", body: JSON.stringify(payload) }),
  update: (id: number, payload: PurchasePayload) =>
    requestJson<PurchaseOrder>(`/purchases/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const salesApi = {
  list: (query: SalesQuery) => requestJson<PageResponse<SalesOrder>>("/sales", {}, query),
  detail: (id: number) => requestJson<SalesOrder>(`/sales/${id}`),
  create: (payload: SalesPayload) =>
    requestJson<SalesOrder>("/sales", { method: "POST", body: JSON.stringify(payload) }),
  update: (id: number, payload: SalesPayload) =>
    requestJson<SalesOrder>(`/sales/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const inventoryApi = {
  list: (query: InventoryQuery) => requestJson<PageResponse<InventoryRow>>("/inventory", {}, query),
  movements: (productId: number, query: PageQuery) =>
    requestJson<PageResponse<StockMovement>>(`/inventory/products/${productId}/movements`, {}, query)
};

export const reportsApi = {
  slowMoving: (query: SlowMovingQuery) =>
    requestJson<PageResponse<SlowMovingRow>>("/reports/slow-moving-products", {}, query)
};
