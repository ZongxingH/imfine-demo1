interface PaginationProps {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
}

export default function Pagination({
  page,
  size,
  totalElements,
  totalPages,
  onPageChange,
  onSizeChange
}: PaginationProps) {
  const canPrev = page > 0;
  const canNext = page + 1 < totalPages;

  return (
    <div className="pagination">
      <span className="muted">共 {totalElements} 条</span>
      <select value={size} onChange={(event) => onSizeChange(Number(event.target.value))}>
        <option value={10}>10 条/页</option>
        <option value={20}>20 条/页</option>
        <option value={50}>50 条/页</option>
      </select>
      <button type="button" className="ghostButton" disabled={!canPrev} onClick={() => onPageChange(page - 1)}>
        上一页
      </button>
      <span>
        第 {totalPages === 0 ? 0 : page + 1} / {totalPages} 页
      </span>
      <button type="button" className="ghostButton" disabled={!canNext} onClick={() => onPageChange(page + 1)}>
        下一页
      </button>
    </div>
  );
}
