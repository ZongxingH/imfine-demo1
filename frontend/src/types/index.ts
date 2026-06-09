export type Status = "ACTIVE" | "INACTIVE";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: string[] | Record<string, string> | Array<Record<string, unknown>>;
  timestamp?: string;
}

export interface Supplier {
  id: number;
  name: string;
  contactName?: string;
  contactPhone?: string;
  address?: string;
  status: Status;
  createdAt?: string;
  updatedAt?: string;
}

export interface SupplierPayload {
  name: string;
  contactName?: string;
  contactPhone?: string;
  address?: string;
  status: Status;
}

export interface ProductCategory {
  id: number;
  name: string;
  description?: string;
  status: Status;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryPayload {
  name: string;
  description?: string;
  status: Status;
}

export interface Product {
  id: number;
  categoryId: number;
  categoryName?: string;
  code: string;
  name: string;
  unit: string;
  shelfLifeDays?: number;
  suggestedSalePrice?: number;
  status: Status;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductPayload {
  categoryId: number;
  code: string;
  name: string;
  unit: string;
  shelfLifeDays?: number;
  suggestedSalePrice?: number;
  status: Status;
}

export interface PurchaseOrderItem {
  id?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  unit?: string;
  quantity: number;
  unitCost: number;
  amount?: number;
}

export interface PurchaseOrder {
  id: number;
  orderNo: string;
  supplierId: number;
  supplierName?: string;
  inboundDate: string;
  totalAmount?: number;
  remark?: string;
  items?: PurchaseOrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchasePayload {
  orderNo: string;
  supplierId: number;
  inboundDate: string;
  remark?: string;
  items: Array<{
    productId: number;
    quantity: number;
    unitCost: number;
  }>;
}

export interface SalesOrderItem {
  id?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  unit?: string;
  quantity: number;
  unitPrice: number;
  amount?: number;
}

export interface SalesOrder {
  id: number;
  orderNo: string;
  customerName?: string;
  outboundDate: string;
  totalAmount?: number;
  remark?: string;
  items?: SalesOrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SalesPayload {
  orderNo: string;
  customerName?: string;
  outboundDate: string;
  remark?: string;
  items: Array<{
    productId: number;
    quantity: number;
    unitPrice: number;
  }>;
}

export interface InventoryRow {
  productId: number;
  productCode: string;
  productName: string;
  categoryId?: number;
  categoryName?: string;
  unit: string;
  currentStock: number;
}

export interface StockMovement {
  id: number;
  movementType: "PURCHASE_IN" | "SALES_OUT";
  sourceType: "PURCHASE_ORDER" | "SALES_ORDER";
  sourceId: number;
  quantityDelta: number;
  movementDate: string;
  createdAt?: string;
}

export interface SlowMovingRow {
  productId: number;
  productCode: string;
  productName: string;
  categoryName?: string;
  unit: string;
  currentStock: number;
  lastSalesDate?: string | null;
  daysSinceLastSale?: number | null;
}
