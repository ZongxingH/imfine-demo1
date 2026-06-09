import { FormEvent, useEffect, useState } from "react";
import { errorMessage } from "../api/http";
import { productsApi, purchasesApi, suppliersApi, type PurchaseQuery } from "../api/resources";
import DataTable, { type Column } from "../components/DataTable";
import { Field, SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import type { PageResponse, Product, PurchaseOrder, PurchasePayload, Supplier } from "../types";
import { collectErrors, maxLength, nonNegative, pastOrToday, positive, required, type FieldErrors } from "../validation/validators";

interface LineForm {
  productId: string;
  quantity: string;
  unitCost: string;
}

const emptyPage: PageResponse<PurchaseOrder> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const newLine = (): LineForm => ({ productId: "", quantity: "", unitCost: "" });
const today = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
};
const initialForm = { orderNo: "", supplierId: "", inboundDate: today(), remark: "", items: [newLine()] };

export default function PurchasesPage() {
  const [query, setQuery] = useState<PurchaseQuery>({ page: 0, size: 10, orderNo: "", supplierId: "", fromDate: "", toDate: "" });
  const [page, setPage] = useState(emptyPage);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(initialForm);
  const [formErrors, setFormErrors] = useState<FieldErrors>({});
  const [saving, setSaving] = useState(false);

  async function loadLookups() {
    try {
      const [supplierPage, productPage] = await Promise.all([
        suppliersApi.list({ page: 0, size: 200, keyword: "", status: "ACTIVE" }),
        productsApi.list({ page: 0, size: 300, keyword: "", categoryId: "", status: "ACTIVE" })
      ]);
      setSuppliers(supplierPage.content);
      setProducts(productPage.content);
    } catch {
      setSuppliers([]);
      setProducts([]);
    }
  }

  async function load() {
    setLoading(true);
    setError("");
    try {
      setPage(await purchasesApi.list(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadLookups();
  }, []);

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.orderNo, query.supplierId, query.fromDate, query.toDate]);

  function validate() {
    const entries: Array<[string, string]> = [
      ["orderNo", required(form.orderNo, "入库单号") || maxLength(form.orderNo, 60, "入库单号")],
      ["supplierId", required(form.supplierId, "供应商")],
      ["inboundDate", required(form.inboundDate, "入库日期") || pastOrToday(form.inboundDate, "入库日期")],
      ["remark", maxLength(form.remark, 255, "备注")]
    ];
    if (form.items.length === 0) {
      entries.push(["items", "至少需要一条入库明细"]);
    }
    form.items.forEach((line, index) => {
      entries.push([`items.${index}.productId`, required(line.productId, "商品")]);
      entries.push([`items.${index}.quantity`, positive(line.quantity, "数量")]);
      entries.push([`items.${index}.unitCost`, nonNegative(line.unitCost, "进价") || required(line.unitCost, "进价")]);
    });
    const errors = collectErrors(entries);
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  function setLine(index: number, patch: Partial<LineForm>) {
    setForm({
      ...form,
      items: form.items.map((line, lineIndex) => (lineIndex === index ? { ...line, ...patch } : line))
    });
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!validate()) return;
    const payload: PurchasePayload = {
      orderNo: form.orderNo.trim(),
      supplierId: Number(form.supplierId),
      inboundDate: form.inboundDate,
      remark: form.remark.trim() || undefined,
      items: form.items.map((line) => ({
        productId: Number(line.productId),
        quantity: Number(line.quantity),
        unitCost: Number(line.unitCost)
      }))
    };
    setSaving(true);
    try {
      if (editingId) {
        await purchasesApi.update(editingId, payload);
      } else {
        await purchasesApi.create(payload);
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
    setForm({ ...initialForm, inboundDate: today(), items: [newLine()] });
    setFormErrors({});
    setModalOpen(true);
  }

  async function openEdit(row: PurchaseOrder) {
    setEditingId(row.id);
    setFormErrors({});
    setModalOpen(true);
    setSaving(true);
    try {
      const detail = await purchasesApi.detail(row.id);
      setForm({
        orderNo: detail.orderNo,
        supplierId: String(detail.supplierId ?? ""),
        inboundDate: detail.inboundDate,
        remark: detail.remark ?? "",
        items: detail.items?.length
          ? detail.items.map((item) => ({
              productId: String(item.productId),
              quantity: String(item.quantity),
              unitCost: String(item.unitCost)
            }))
          : [newLine()]
      });
    } catch (err) {
      setFormErrors({ form: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  }

  const supplierOptions = suppliers.map((supplier) => ({ label: supplier.name, value: supplier.id }));
  const productOptions = products.map((product) => ({ label: `${product.code} ${product.name}`, value: product.id }));
  const columns: Column<PurchaseOrder>[] = [
    { header: "入库单号", render: (row) => row.orderNo, width: "18%" },
    { header: "供应商", render: (row) => row.supplierName || row.supplierId, width: "18%" },
    { header: "入库日期", render: (row) => row.inboundDate, width: "14%" },
    {
      header: "总金额",
      render: (row) => (row.totalAmount === undefined ? "-" : row.totalAmount.toFixed(2)),
      width: "14%",
      align: "right"
    },
    { header: "备注", render: (row) => row.remark || "-", width: "24%" },
    {
      header: "操作",
      width: "12%",
      render: (row) => (
        <button type="button" className="linkButton" onClick={() => void openEdit(row)}>
          编辑
        </button>
      )
    }
  ];

  return (
    <>
      <PageHeader
        title="采购入库"
        description="登记采购入库单，明细保存后由后端生成库存增加流水。"
        action={
          <button type="button" className="primaryButton" onClick={openCreate}>
            新增入库单
          </button>
        }
      />
      <section className="toolbar toolbarWide">
        <input
          placeholder="入库单号"
          value={query.orderNo}
          onChange={(event) => setQuery({ ...query, orderNo: event.target.value, page: 0 })}
        />
        <SelectField
          value={query.supplierId}
          onChange={(event) => setQuery({ ...query, supplierId: event.target.value ? Number(event.target.value) : "", page: 0 })}
          options={supplierOptions}
          placeholder="全部供应商"
        />
        <input type="date" value={query.fromDate} onChange={(event) => setQuery({ ...query, fromDate: event.target.value, page: 0 })} />
        <input type="date" value={query.toDate} onChange={(event) => setQuery({ ...query, toDate: event.target.value, page: 0 })} />
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
        <Modal title={editingId ? "编辑入库单" : "新增入库单"} onClose={() => setModalOpen(false)} width="wide">
          <form className="formGrid documentForm" onSubmit={submit}>
            {formErrors.form && <div className="formError">{formErrors.form}</div>}
            <Field label="入库单号" error={formErrors.orderNo}>
              <input value={form.orderNo} onChange={(event) => setForm({ ...form, orderNo: event.target.value })} />
            </Field>
            <Field label="供应商" error={formErrors.supplierId}>
              <SelectField
                value={form.supplierId}
                onChange={(event) => setForm({ ...form, supplierId: event.target.value })}
                options={supplierOptions}
                placeholder="请选择供应商"
              />
            </Field>
            <Field label="入库日期" error={formErrors.inboundDate}>
              <input type="date" value={form.inboundDate} onChange={(event) => setForm({ ...form, inboundDate: event.target.value })} />
            </Field>
            <Field label="备注" error={formErrors.remark}>
              <input value={form.remark} onChange={(event) => setForm({ ...form, remark: event.target.value })} />
            </Field>
            <div className="lineEditor">
              <div className="lineEditorHeader">
                <strong>入库明细</strong>
                <button type="button" className="ghostButton" onClick={() => setForm({ ...form, items: [...form.items, newLine()] })}>
                  添加明细
                </button>
              </div>
              {formErrors.items && <div className="formError">{formErrors.items}</div>}
              {form.items.map((line, index) => (
                <div className="lineRow" key={index}>
                  <Field label="商品" error={formErrors[`items.${index}.productId`]}>
                    <SelectField
                      value={line.productId}
                      onChange={(event) => setLine(index, { productId: event.target.value })}
                      options={productOptions}
                      placeholder="请选择商品"
                    />
                  </Field>
                  <Field label="数量" error={formErrors[`items.${index}.quantity`]}>
                    <input type="number" min="0" step="0.001" value={line.quantity} onChange={(event) => setLine(index, { quantity: event.target.value })} />
                  </Field>
                  <Field label="进价" error={formErrors[`items.${index}.unitCost`]}>
                    <input type="number" min="0" step="0.01" value={line.unitCost} onChange={(event) => setLine(index, { unitCost: event.target.value })} />
                  </Field>
                  <div className="lineTotal">
                    {(Number(line.quantity || 0) * Number(line.unitCost || 0)).toFixed(2)}
                  </div>
                  <button
                    type="button"
                    className="linkButton dangerText"
                    onClick={() => setForm({ ...form, items: form.items.filter((_, lineIndex) => lineIndex !== index) })}
                  >
                    删除
                  </button>
                </div>
              ))}
            </div>
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
