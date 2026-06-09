import type { Status } from "../types";

const labels: Record<Status, string> = {
  ACTIVE: "启用",
  INACTIVE: "停用"
};

export default function StatusBadge({ status }: { status: Status }) {
  return <span className={`statusBadge ${status === "ACTIVE" ? "statusActive" : "statusInactive"}`}>{labels[status]}</span>;
}
