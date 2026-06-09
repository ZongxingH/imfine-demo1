import type { ReactNode } from "react";

export interface Column<T> {
  header: string;
  render: (row: T) => ReactNode;
  width?: string;
  align?: "left" | "right" | "center";
}

interface DataTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  rowKey: (row: T) => string | number;
  loading?: boolean;
  error?: string;
  emptyText?: string;
}

export default function DataTable<T>({
  columns,
  rows,
  rowKey,
  loading,
  error,
  emptyText = "暂无数据"
}: DataTableProps<T>) {
  return (
    <div className="tableWrap">
      <table className="dataTable">
        <colgroup>
          {columns.map((column) => (
            <col key={column.header} style={{ width: column.width }} />
          ))}
        </colgroup>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.header} className={column.align ? `align-${column.align}` : undefined}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr>
              <td className="tableState" colSpan={columns.length}>
                正在加载数据...
              </td>
            </tr>
          )}
          {!loading && error && (
            <tr>
              <td className="tableState errorText" colSpan={columns.length}>
                {error}
              </td>
            </tr>
          )}
          {!loading && !error && rows.length === 0 && (
            <tr>
              <td className="tableState muted" colSpan={columns.length}>
                {emptyText}
              </td>
            </tr>
          )}
          {!loading &&
            !error &&
            rows.map((row) => (
              <tr key={rowKey(row)}>
                {columns.map((column) => (
                  <td key={column.header} className={column.align ? `align-${column.align}` : undefined}>
                    {column.render(row)}
                  </td>
                ))}
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
}
