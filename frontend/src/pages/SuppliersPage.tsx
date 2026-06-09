import { FormEvent, useEffect, useState } from "react";
import { suppliersApi, type StatusQuery } from "../api/resources";
import { errorMessage } from "../api/http";
import DataTable, { type Column } from "../components/DataTable";
import { Field, SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import StatusBadge from "../components/StatusBadge";
import type { PageResponse, Status, Supplier, SupplierPayload } from "../types";
import { collectErrors, maxLength, phone, required, type FieldErrors } from "../validation/validators";

const emptyPage: PageResponse<Supplier> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const initialForm = { name: "", contactName: "", contactPhone: "", address: "", status: "ACTIVE" as Status };

export default function SuppliersPage() {
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
      setPage(await suppliersApi.list(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.keyword, query.status]);

  function openCreate() {
    setEditingId(null);
    setForm(initialForm);
    setFormErrors({});
    setModalOpen(true);
  }

  function openEdit(row: Supplier) {
    setEditingId(row.id);
    setForm({
      name: row.name ?? "",
      contactName: row.contactName ?? "",
      contactPhone: row.contactPhone ?? "",
      address: row.address ?? "",
      status: row.status
    });
    setFormErrors({});
    setModalOpen(true);
  }

  function validate() {
    const errors = collectErrors([
      ["name", required(form.name, "供应商名称") || maxLength(form.name, 120, "供应商名称")],
      ["contactName", maxLength(form.contactName, 80, "联系人")],
      ["contactPhone", phone(form.contactPhone) || maxLength(form.contactPhone, 40, "联系电话")],
      ["address", maxLength(form.address, 255, "地址")]
    ]);
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!validate()) return;
    const payload: SupplierPayload = {
      name: form.name.trim(),
      contactName: form.contactName.trim() || undefined,
      contactPhone: form.contactPhone.trim() || undefined,
      address: form.address.trim() || undefined,
      status: form.status
    };
    setSaving(true);
    try {
      if (editingId) {
        await suppliersApi.update(editingId, payload);
      } else {
        await suppliersApi.create(payload);
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setFormErrors({ form: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  }

  const columns: Column<Supplier>[] = [
    { header: "供应商名称", render: (row) => row.name, width: "22%" },
    { header: "联系人", render: (row) => row.contactName || "-", width: "14%" },
    { header: "联系电话", render: (row) => row.contactPhone || "-", width: "16%" },
    { header: "地址", render: (row) => row.address || "-", width: "26%" },
    { header: "状态", render: (row) => <StatusBadge status={row.status} />, width: "10%" },
    {
      header: "操作",
      width: "12%",
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
        title="供应商档案"
        description="维护采购入库可选供应商，停用后不再用于新增单据。"
        action={
          <button type="button" className="primaryButton" onClick={openCreate}>
            新增供应商
          </button>
        }
      />
      <section className="toolbar">
        <input
          placeholder="按名称、联系人搜索"
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
        <Modal title={editingId ? "编辑供应商" : "新增供应商"} onClose={() => setModalOpen(false)}>
          <form className="formGrid" onSubmit={submit}>
            {formErrors.form && <div className="formError">{formErrors.form}</div>}
            <Field label="供应商名称" error={formErrors.name}>
              <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            </Field>
            <Field label="联系人" error={formErrors.contactName}>
              <input value={form.contactName} onChange={(event) => setForm({ ...form, contactName: event.target.value })} />
            </Field>
            <Field label="联系电话" error={formErrors.contactPhone}>
              <input value={form.contactPhone} onChange={(event) => setForm({ ...form, contactPhone: event.target.value })} />
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
            <Field label="地址" error={formErrors.address}>
              <textarea value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} />
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
