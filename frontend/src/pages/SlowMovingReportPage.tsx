import { useEffect, useState } from "react";
import { errorMessage } from "../api/http";
import { categoriesApi, reportsApi, type SlowMovingQuery } from "../api/resources";
import DataTable, { type Column } from "../components/DataTable";
import { SelectField } from "../components/FormControls";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import type { PageResponse, ProductCategory, SlowMovingRow } from "../types";

const emptyPage: PageResponse<SlowMovingRow> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

export default function SlowMovingReportPage() {
  const [query, setQuery] = useState<SlowMovingQuery>({ page: 0, size: 10, keyword: "", categoryId: "", days: 30 });
  const [page, setPage] = useState(emptyPage);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

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
      setPage(await reportsApi.slowMoving(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadCategories();
  }, []);

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.keyword, query.categoryId, query.days]);

  const categoryOptions = categories.map((category) => ({ label: category.name, value: category.id }));
  const columns: Column<SlowMovingRow>[] = [
    { header: "商品编码", render: (row) => row.productCode, width: "14%" },
    { header: "商品名称", render: (row) => row.productName, width: "22%" },
    { header: "品类", render: (row) => row.categoryName || "-", width: "16%" },
    { header: "单位", render: (row) => row.unit, width: "8%" },
    { header: "当前库存", render: (row) => row.currentStock.toFixed(3), width: "14%", align: "right" },
    { header: "最近销售日", render: (row) => row.lastSalesDate || "无销售记录", width: "14%" },
    {
      header: "未售天数",
      render: (row) => (row.daysSinceLastSale === null || row.daysSinceLastSale === undefined ? "-" : row.daysSinceLastSale),
      width: "12%",
      align: "right"
    }
  ];

  return (
    <>
      <PageHeader title="滞销统计" description="查询有库存且超过指定天数未销售的商品。" />
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
        <input
          type="number"
          min="1"
          step="1"
          value={query.days}
          onChange={(event) => setQuery({ ...query, days: Math.max(1, Number(event.target.value || 1)), page: 0 })}
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
    </>
  );
}
