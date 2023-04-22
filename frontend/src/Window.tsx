import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { CSSProperties, ReactNode, useEffect, useMemo } from "react";
import "./Window.css";

type WindowProps = {
  title: string;
  onClose: () => void;
  className?: string;
  style?: CSSProperties;
  children: ReactNode;
};

export default function Window({ title, onClose, className, style, children }: WindowProps) {
  const oldScrollTop = useMemo(() => document.documentElement.scrollTop, []);

  useEffect(() => {
    const header = document.querySelector<HTMLElement>(".Header");

    document.body.style.top = -oldScrollTop + "px";
    if (header) header.style.top = oldScrollTop + "px";

    return () => {
      document.documentElement.scrollTop = oldScrollTop;
      if (header) header.style.top = "";
    };
  }, []);

  return (
    <div className="Window__Background" onClick={onClose}>
      <div className={`Window ${className}`} style={style} onClick={(e) => e.stopPropagation()}>
        <h3>
          {title}
          <FontAwesomeIcon
            icon={faXmark}
            title="Close"
            role="button"
            cursor="pointer"
            onClick={onClose}
            className="Window__CloseButton"
          />
        </h3>
        <div className="Window__Children">{children}</div>
      </div>
    </div>
  );
}
