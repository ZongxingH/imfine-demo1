import { useEffect, useState } from "react";
import { errorMessage } from "../api/http";
import { categoriesApi, inventoryApi, type InventoryQuery } from "../api/resources";
import DataTable, { type Column } from "../components/DataTable";
import { SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import type { InventoryRow, PageResponse, ProductCategory, StockMovement } from "../types";

const emptyPage: PageResponse<InventoryRow> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const emptyMovementPage: PageResponse<StockMovement> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

export default function InventoryPage() {
  const [query, setQuery] = useState<InventoryQuery>({ page: 0, size: 10, keyword: "", categoryId: "" });
  const [page, setPage] = useState(emptyPage);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [movementProduct, setMovementProduct] = useState<InventoryRow | null>(null);
  const [movementPage, setMovementPage] = useState(emptyMovementPage);
  const [movementLoading, setMovementLoading] = useState(false);
  const [movementError, setMovementError] = useState("");

  async function loadCategories() {
    try {
      const result = await categoriesApi.list({ page: 0, size: 200, keyword: "", status: "" });
      setCategories(result.content);
    } catch {
      setCategories([]);
    }
  }

  async function load() {
    setLoading(true);
    setError("");
    try {
      setPage(await inventoryApi.list(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function loadMovements(product: InventoryRow, pageNumber = 0, size = movementPage.size) {
    setMovementProduct(product);
    setMovementLoading(true);
    setMovementError("");
    try {
      setMovementPage(await inventoryApi.movements(product.productId, { page: pageNumber, size }));
    } catch (err) {
      setMovementError(errorMessage(err));
    } finally {
      setMovementLoading(false);
    }
  }

  useEffect(() => {
    void loadCategories();
  }, []);

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.keyword, query.categoryId]);

  const categoryOptions = categories.map((category) => ({ label: category.name, value: category.id }));
  const columns: Column<InventoryRow>[] = [
    { header: "商品编码", render: (row) => row.productCode, width: "16%" },
    { header: "商品名称", render: (row) => row.productName, width: "24%" },
    { header: "品类", render: (row) => row.categoryName || "-", width: "18%" },
    { header: "单位", render: (row) => row.unit, width: "10%" },
    { header: "当前库存", render: (row) => row.currentStock.toFixed(3), width: "16%", align: "right" },
    {
      header: "操作",
      render: (row) => (
        <button type="button" className="linkButton" onClick={() => void loadMovements(row)}>
          查看流水
        </button>
      ),
      width: "16%"
    }
  ];
  const movementColumns: Column<StockMovement>[] = [
    { header: "日期", render: (row) => row.movementDate, width: "18%" },
    { header: "类型", render: (row) => (row.movementType === "PURCHASE_IN" ? "采购入库" : "销售出库"), width: "18%" },
    { header: "来源", render: (row) => `${row.sourceType} #${row.sourceId}`, width: "28%" },
    { header: "数量变化", render: (row) => row.quantityDelta.toFixed(3), width: "18%", align: "right" },
    { header: "创建时间", render: (row) => row.createdAt || "-", width: "18%" }
  ];

  return (
    <>
      <PageHeader title="实时库存" description="按商品查询当前库存，库存由出入库流水汇总得到。" />
      <section className="toolbar toolbarWide">
        <input
          placeholder="按商品编码或名称搜索"
          value={query.keyword}
          onChange={(event) => setQuery({ ...query, keyword: event.target.value, page: 0 })}
        />
        <SelectField
          value={query.categoryId}
          onChange={(event) => setQuery({ ...query, categoryId: event.target.value ? Number(event.target.value) : "", page: 0 })}
          options={categoryOptions}
          placeholder="全部品类"
        />
        <button type="button" className="ghostButton" onClick={load}>
          查询
        </button>
      </section>
      <DataTable columns={columns} rows={page.content} rowKey={(row) => row.productId} loading={loading} error={error} />
      <Pagination
        page={page.page}
        size={page.size}
        totalElements={page.totalElements}
        totalPages={page.totalPages}
        onPageChange={(nextPage) => setQuery({ ...query, page: nextPage })}
        onSizeChange={(size) => setQuery({ ...query, size, page: 0 })}
      />

      {movementProduct && (
        <Modal title={`${movementProduct.productName} 库存流水`} onClose={() => setMovementProduct(null)} width="wide">
          <DataTable
            columns={movementColumns}
            rows={movementPage.content}
            rowKey={(row) => row.id}
            loading={movementLoading}
            error={movementError}
            emptyText="暂无库存流水"
          />
          <Pagination
            page={movementPage.page}
            size={movementPage.size}
            totalElements={movementPage.totalElements}
            totalPages={movementPage.totalPages}
            onPageChange={(nextPage) => void loadMovements(movementProduct, nextPage)}
            onSizeChange={(size) => void loadMovements(movementProduct, 0, size)}
          />
        </Modal>
      )}
    </>
  );
}
