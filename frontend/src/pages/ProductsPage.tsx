import { FormEvent, useEffect, useState } from "react";
import { categoriesApi, productsApi, type ProductQuery } from "../api/resources";
import { errorMessage } from "../api/http";
import DataTable, { type Column } from "../components/DataTable";
import { Field, SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import StatusBadge from "../components/StatusBadge";
import type { PageResponse, Product, ProductCategory, ProductPayload, Status } from "../types";
import {
  collectErrors,
  maxLength,
  nonNegative,
  positiveInteger,
  required,
  type FieldErrors
} from "../validation/validators";

const emptyPage: PageResponse<Product> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const initialForm = {
  categoryId: "",
  code: "",
  name: "",
  unit: "",
  shelfLifeDays: "",
  suggestedSalePrice: "",
  status: "ACTIVE" as Status
};

export default function ProductsPage() {
  const [query, setQuery] = useState<ProductQuery>({ page: 0, size: 10, keyword: "", categoryId: "", status: "" });
  const [page, setPage] = useState(emptyPage);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(initialForm);
  const [formErrors, setFormErrors] = useState<FieldErrors>({});
  const [saving, setSaving] = useState(false);

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
      setPage(await productsApi.list(query));
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
  }, [query.page, query.size, query.keyword, query.categoryId, query.status]);

  function validate() {
    const errors = collectErrors([
      ["categoryId", required(form.categoryId, "商品品类")],
      ["code", required(form.code, "商品编码") || maxLength(form.code, 60, "商品编码")],
      ["name", required(form.name, "商品名称") || maxLength(form.name, 160, "商品名称")],
      ["unit", required(form.unit, "计量单位") || maxLength(form.unit, 20, "计量单位")],
      ["shelfLifeDays", positiveInteger(form.shelfLifeDays, "保质期天数")],
      ["suggestedSalePrice", nonNegative(form.suggestedSalePrice, "建议售价")]
    ]);
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!validate()) return;
    const payload: ProductPayload = {
      categoryId: Number(form.categoryId),
      code: form.code.trim(),
      name: form.name.trim(),
      unit: form.unit.trim(),
      shelfLifeDays: form.shelfLifeDays === "" ? undefined : Number(form.shelfLifeDays),
      suggestedSalePrice: form.suggestedSalePrice === "" ? undefined : Number(form.suggestedSalePrice),
      status: form.status
    };
    setSaving(true);
    try {
      if (editingId) {
        await productsApi.update(editingId, payload);
      } else {
        await productsApi.create(payload);
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setFormErrors({ form: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  }

  function openCreate() {
    setEditingId(null);
    setForm(initialForm);
    setFormErrors({});
    setModalOpen(true);
  }

  function openEdit(row: Product) {
    setEditingId(row.id);
    setForm({
      categoryId: String(row.categoryId ?? ""),
      code: row.code ?? "",
      name: row.name ?? "",
      unit: row.unit ?? "",
      shelfLifeDays: row.shelfLifeDays === undefined ? "" : String(row.shelfLifeDays),
      suggestedSalePrice: row.suggestedSalePrice === undefined ? "" : String(row.suggestedSalePrice),
      status: row.status
    });
    setFormErrors({});
    setModalOpen(true);
  }

  const categoryOptions = categories.map((category) => ({ label: category.name, value: category.id }));
  const columns: Column<Product>[] = [
    { header: "编码", render: (row) => row.code, width: "13%" },
    { header: "商品名称", render: (row) => row.name, width: "20%" },
    { header: "品类", render: (row) => row.categoryName || row.categoryId, width: "14%" },
    { header: "单位", render: (row) => row.unit, width: "8%" },
    { header: "保质期", render: (row) => (row.shelfLifeDays ? `${row.shelfLifeDays} 天` : "-"), width: "10%" },
    {
      header: "建议售价",
      render: (row) => (row.suggestedSalePrice === undefined ? "-" : row.suggestedSalePrice.toFixed(2)),
      width: "12%",
      align: "right"
    },
    { header: "状态", render: (row) => <StatusBadge status={row.status} />, width: "10%" },
    {
      header: "操作",
      width: "13%",
      render: (row) => (
        <button type="button" className="linkButton" onClick={() => openEdit(row)}>
          编辑
        </button>
      )
    }
  ];

  return (
    <>
      <PageHeader
        title="商品档案"
        description="维护商品编码、单位、保质期和售价，供出入库单据选用。"
        action={
          <button type="button" className="primaryButton" onClick={openCreate}>
            新增商品
          </button>
        }
      />
      <section className="toolbar toolbarWide">
        <input
          placeholder="按编码或名称搜索"
          value={query.keyword}
          onChange={(event) => setQuery({ ...query, keyword: event.target.value, page: 0 })}
        />
        <SelectField
          value={query.categoryId}
          onChange={(event) => setQuery({ ...query, categoryId: event.target.value ? Number(event.target.value) : "", page: 0 })}
          options={categoryOptions}
          placeholder="全部品类"
        />
        <SelectField
          value={query.status}
          onChange={(event) => setQuery({ ...query, status: event.target.value as Status | "", page: 0 })}
          options={[
            { label: "启用", value: "ACTIVE" },
            { label: "停用", value: "INACTIVE" }
          ]}
          placeholder="全部状态"
        />
        <button type="button" className="ghostButton" onClick={load}>
          查询
        </button>
      </section>
      <DataTable columns={columns} rows={page.content} rowKey={(row) => row.id} loading={loading} error={error} />
      <Pagination
        page={page.page}
        size={page.size}
        totalElements={page.totalElements}
        totalPages={page.totalPages}
        onPageChange={(nextPage) => setQuery({ ...query, page: nextPage })}
        onSizeChange={(size) => setQuery({ ...query, size, page: 0 })}
      />

      {modalOpen && (
        <Modal title={editingId ? "编辑商品" : "新增商品"} onClose={() => setModalOpen(false)}>
          <form className="formGrid" onSubmit={submit}>
            {formErrors.form && <div className="formError">{formErrors.form}</div>}
            <Field label="商品品类" error={formErrors.categoryId}>
              <SelectField
                value={form.categoryId}
                onChange={(event) => setForm({ ...form, categoryId: event.target.value })}
                options={categoryOptions}
                placeholder="请选择品类"
              />
            </Field>
            <Field label="商品编码" error={formErrors.code}>
              <input value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
            </Field>
            <Field label="商品名称" error={formErrors.name}>
              <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            </Field>
            <Field label="计量单位" error={formErrors.unit}>
              <input value={form.unit} onChange={(event) => setForm({ ...form, unit: event.target.value })} />
            </Field>
            <Field label="保质期天数" error={formErrors.shelfLifeDays}>
              <input
                type="number"
                min="1"
                step="1"
                value={form.shelfLifeDays}
                onChange={(event) => setForm({ ...form, shelfLifeDays: event.target.value })}
              />
            </Field>
            <Field label="建议售价" error={formErrors.suggestedSalePrice}>
              <input
                type="number"
                min="0"
                step="0.01"
                value={form.suggestedSalePrice}
                onChange={(event) => setForm({ ...form, suggestedSalePrice: event.target.value })}
              />
            </Field>
            <Field label="状态">
              <SelectField
                value={form.status}
                onChange={(event) => setForm({ ...form, status: event.target.value as Status })}
                options={[
                  { label: "启用", value: "ACTIVE" },
                  { label: "停用", value: "INACTIVE" }
                ]}
              />
            </Field>
            <footer className="modalActions">
              <button type="button" className="ghostButton" onClick={() => setModalOpen(false)}>
                取消
              </button>
              <button type="submit" className="primaryButton" disabled={saving}>
                {saving ? "保存中..." : "保存"}
              </button>
            </footer>
          </form>
        </Modal>
      )}
    </>
  );
}
