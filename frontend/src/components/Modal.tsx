import type { ReactNode } from "react";

interface ModalProps {
  title: string;
  children: ReactNode;
  onClose: () => void;
  width?: "normal" | "wide";
}

export default function Modal({ title, children, onClose, width = "normal" }: ModalProps) {
  return (
    <div className="modalOverlay" role="presentation">
      <section className={`modal ${width === "wide" ? "modalWide" : ""}`} role="dialog" aria-modal="true">
        <header className="modalHeader">
          <h2>{title}</h2>
          <button type="button" className="iconButton" aria-label="关闭" onClick={onClose}>
            x
          </button>
        </header>
        {children}
      </section>
    </div>
  );
}
