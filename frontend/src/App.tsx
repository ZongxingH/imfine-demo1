import { useEffect, useMemo, useState } from "react";
import CategoriesPage from "./pages/CategoriesPage";
import InventoryPage from "./pages/InventoryPage";
import ProductsPage from "./pages/ProductsPage";
import PurchasesPage from "./pages/PurchasesPage";
import SalesPage from "./pages/SalesPage";
import SlowMovingReportPage from "./pages/SlowMovingReportPage";
import SuppliersPage from "./pages/SuppliersPage";

interface NavItem {
  path: string;
  label: string;
  group: string;
}

const navItems: NavItem[] = [
  { path: "/suppliers", label: "供应商档案", group: "基础档案" },
  { path: "/categories", label: "商品品类", group: "基础档案" },
  { path: "/products", label: "商品档案", group: "基础档案" },
  { path: "/purchases", label: "采购入库", group: "业务单据" },
  { path: "/sales", label: "销售出库", group: "业务单据" },
  { path: "/inventory", label: "实时库存", group: "库存报表" },
  { path: "/reports/slow-moving", label: "滞销统计", group: "库存报表" }
];

function currentPath() {
  const path = window.location.pathname;
  return navItems.some((item) => item.path === path) ? path : "/suppliers";
}

export default function App() {
  const [path, setPath] = useState(currentPath);
  const groups = useMemo(() => Array.from(new Set(navItems.map((item) => item.group))), []);

  useEffect(() => {
    if (window.location.pathname === "/") {
      window.history.replaceState(null, "", "/suppliers");
    }
    const onPopState = () => setPath(currentPath());
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  function navigate(nextPath: string) {
    window.history.pushState(null, "", nextPath);
    setPath(nextPath);
  }

  function renderPage() {
    switch (path) {
      case "/categories":
        return <CategoriesPage />;
      case "/products":
        return <ProductsPage />;
      case "/purchases":
        return <PurchasesPage />;
      case "/sales":
        return <SalesPage />;
      case "/inventory":
        return <InventoryPage />;
      case "/reports/slow-moving":
        return <SlowMovingReportPage />;
      case "/suppliers":
      default:
        return <SuppliersPage />;
    }
  }

  return (
    <div className="appShell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brandMark">鲜</span>
          <div>
            <strong>生鲜进销存</strong>
            <small>管理控制台</small>
          </div>
        </div>
        <nav>
          {groups.map((group) => (
            <div className="navGroup" key={group}>
              <div className="navGroupTitle">{group}</div>
              {navItems
                .filter((item) => item.group === group)
                .map((item) => (
                  <button
                    type="button"
                    className={path === item.path ? "navItem active" : "navItem"}
                    key={item.path}
                    onClick={() => navigate(item.path)}
                  >
                    {item.label}
                  </button>
                ))}
            </div>
          ))}
        </nav>
      </aside>
      <main className="mainContent">{renderPage()}</main>
    </div>
  );
}
