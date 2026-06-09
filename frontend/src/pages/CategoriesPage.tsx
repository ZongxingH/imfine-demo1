import { FormEvent, useEffect, useState } from "react";
import { categoriesApi, type StatusQuery } from "../api/resources";
import { errorMessage } from "../api/http";
import DataTable, { type Column } from "../components/DataTable";
import { Field, SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import StatusBadge from "../components/StatusBadge";
import type { CategoryPayload, PageResponse, ProductCategory, Status } from "../types";
import { collectErrors, maxLength, required, type FieldErrors } from "../validation/validators";

const emptyPage: PageResponse<ProductCategory> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const initialForm = { name: "", description: "", status: "ACTIVE" as Status };

export default function CategoriesPage() {
  const [query, setQuery] = useState<StatusQuery>({ page: 0, size: 10, keyword: "", status: "" });
  const [page, setPage] = useState(emptyPage);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(initialForm);
  const [formErrors, setFormErrors] = useState<FieldErrors>({});
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    setError("");
    try {
      setPage(await categoriesApi.list(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.keyword, query.status]);

  function validate() {
    const errors = collectErrors([
      ["name", required(form.name, "品类名称") || maxLength(form.name, 120, "品类名称")],
      ["description", maxLength(form.description, 255, "描述")]
    ]);
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!validate()) return;
    const payload: CategoryPayload = {
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      status: form.status
    };
    setSaving(true);
    try {
      if (editingId) {
        await categoriesApi.update(editingId, payload);
      } else {
        await categoriesApi.create(payload);
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

  function openEdit(row: ProductCategory) {
    setEditingId(row.id);
    setForm({ name: row.name, description: row.description ?? "", status: row.status });
    setFormErrors({});
    setModalOpen(true);
  }

  const columns: Column<ProductCategory>[] = [
    { header: "品类名称", render: (row) => row.name, width: "30%" },
    { header: "描述", render: (row) => row.description || "-", width: "42%" },
    { header: "状态", render: (row) => <StatusBadge status={row.status} />, width: "12%" },
    {
      header: "操作",
      width: "16%",
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
        title="商品品类"
        description="维护生鲜商品分类，商品建档时需要选择有效品类。"
        action={
          <button type="button" className="primaryButton" onClick={openCreate}>
            新增品类
          </button>
        }
      />
      <section className="toolbar">
        <input
          placeholder="按品类名称搜索"
          value={query.keyword}
          onChange={(event) => setQuery({ ...query, keyword: event.target.value, page: 0 })}
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
        <Modal title={editingId ? "编辑品类" : "新增品类"} onClose={() => setModalOpen(false)}>
          <form className="formGrid" onSubmit={submit}>
            {formErrors.form && <div className="formError">{formErrors.form}</div>}
            <Field label="品类名称" error={formErrors.name}>
              <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
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
            <Field label="描述" error={formErrors.description}>
              <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
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
