import { FormEvent, useEffect, useState } from "react";
import { errorMessage } from "../api/http";
import { productsApi, salesApi, type SalesQuery } from "../api/resources";
import DataTable, { type Column } from "../components/DataTable";
import { Field, SelectField } from "../components/FormControls";
import Modal from "../components/Modal";
import PageHeader from "../components/PageHeader";
import Pagination from "../components/Pagination";
import type { PageResponse, Product, SalesOrder, SalesPayload } from "../types";
import { collectErrors, maxLength, nonNegative, pastOrToday, positive, required, type FieldErrors } from "../validation/validators";

interface LineForm {
  productId: string;
  quantity: string;
  unitPrice: string;
}

const emptyPage: PageResponse<SalesOrder> = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
const newLine = (): LineForm => ({ productId: "", quantity: "", unitPrice: "" });
const today = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
};
const initialForm = { orderNo: "", customerName: "", outboundDate: today(), remark: "", items: [newLine()] };

export default function SalesPage() {
  const [query, setQuery] = useState<SalesQuery>({ page: 0, size: 10, orderNo: "", customerName: "", fromDate: "", toDate: "" });
  const [page, setPage] = useState(emptyPage);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(initialForm);
  const [formErrors, setFormErrors] = useState<FieldErrors>({});
  const [saving, setSaving] = useState(false);

  async function loadProducts() {
    try {
      const result = await productsApi.list({ page: 0, size: 300, keyword: "", categoryId: "", status: "ACTIVE" });
      setProducts(result.content);
    } catch {
      setProducts([]);
    }
  }

  async function load() {
    setLoading(true);
    setError("");
    try {
      setPage(await salesApi.list(query));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadProducts();
  }, []);

  useEffect(() => {
    void load();
  }, [query.page, query.size, query.orderNo, query.customerName, query.fromDate, query.toDate]);

  function validate() {
    const entries: Array<[string, string]> = [
      ["orderNo", required(form.orderNo, "销售单号") || maxLength(form.orderNo, 60, "销售单号")],
      ["customerName", maxLength(form.customerName, 120, "客户名称")],
      ["outboundDate", required(form.outboundDate, "出库日期") || pastOrToday(form.outboundDate, "出库日期")],
      ["remark", maxLength(form.remark, 255, "备注")]
    ];
    if (form.items.length === 0) {
      entries.push(["items", "至少需要一条销售明细"]);
    }
    form.items.forEach((line, index) => {
      entries.push([`items.${index}.productId`, required(line.productId, "商品")]);
      entries.push([`items.${index}.quantity`, positive(line.quantity, "数量")]);
      entries.push([`items.${index}.unitPrice`, nonNegative(line.unitPrice, "售价") || required(line.unitPrice, "售价")]);
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
    const payload: SalesPayload = {
      orderNo: form.orderNo.trim(),
      customerName: form.customerName.trim() || undefined,
      outboundDate: form.outboundDate,
      remark: form.remark.trim() || undefined,
      items: form.items.map((line) => ({
        productId: Number(line.productId),
        quantity: Number(line.quantity),
        unitPrice: Number(line.unitPrice)
      }))
    };
    setSaving(true);
    try {
      if (editingId) {
        await salesApi.update(editingId, payload);
      } else {
        await salesApi.create(payload);
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
    setForm({ ...initialForm, outboundDate: today(), items: [newLine()] });
    setFormErrors({});
    setModalOpen(true);
  }

  async function openEdit(row: SalesOrder) {
    setEditingId(row.id);
    setFormErrors({});
    setModalOpen(true);
    setSaving(true);
    try {
      const detail = await salesApi.detail(row.id);
      setForm({
        orderNo: detail.orderNo,
        customerName: detail.customerName ?? "",
        outboundDate: detail.outboundDate,
        remark: detail.remark ?? "",
        items: detail.items?.length
          ? detail.items.map((item) => ({
              productId: String(item.productId),
              quantity: String(item.quantity),
              unitPrice: String(item.unitPrice)
            }))
          : [newLine()]
      });
    } catch (err) {
      setFormErrors({ form: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  }

  const productOptions = products.map((product) => ({ label: `${product.code} ${product.name}`, value: product.id }));
  const columns: Column<SalesOrder>[] = [
    { header: "销售单号", render: (row) => row.orderNo, width: "18%" },
    { header: "客户", render: (row) => row.customerName || "-", width: "18%" },
    { header: "出库日期", render: (row) => row.outboundDate, width: "14%" },
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
        title="销售开单出库"
        description="登记销售出库单，后端校验库存并生成扣减流水。"
        action={
          <button type="button" className="primaryButton" onClick={openCreate}>
            新增销售单
          </button>
        }
      />
      <section className="toolbar toolbarWide">
        <input
          placeholder="销售单号"
          value={query.orderNo}
          onChange={(event) => setQuery({ ...query, orderNo: event.target.value, page: 0 })}
        />
        <input
          placeholder="客户名称"
          value={query.customerName}
          onChange={(event) => setQuery({ ...query, customerName: event.target.value, page: 0 })}
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
        <Modal title={editingId ? "编辑销售单" : "新增销售单"} onClose={() => setModalOpen(false)} width="wide">
          <form className="formGrid documentForm" onSubmit={submit}>
            {formErrors.form && <div className="formError">{formErrors.form}</div>}
            <Field label="销售单号" error={formErrors.orderNo}>
              <input value={form.orderNo} onChange={(event) => setForm({ ...form, orderNo: event.target.value })} />
            </Field>
            <Field label="客户名称" error={formErrors.customerName}>
              <input value={form.customerName} onChange={(event) => setForm({ ...form, customerName: event.target.value })} />
            </Field>
            <Field label="出库日期" error={formErrors.outboundDate}>
              <input type="date" value={form.outboundDate} onChange={(event) => setForm({ ...form, outboundDate: event.target.value })} />
            </Field>
            <Field label="备注" error={formErrors.remark}>
              <input value={form.remark} onChange={(event) => setForm({ ...form, remark: event.target.value })} />
            </Field>
            <div className="lineEditor">
              <div className="lineEditorHeader">
                <strong>销售明细</strong>
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
                  <Field label="售价" error={formErrors[`items.${index}.unitPrice`]}>
                    <input type="number" min="0" step="0.01" value={line.unitPrice} onChange={(event) => setLine(index, { unitPrice: event.target.value })} />
                  </Field>
                  <div className="lineTotal">
                    {(Number(line.quantity || 0) * Number(line.unitPrice || 0)).toFixed(2)}
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
