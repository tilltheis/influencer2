import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { CSSProperties, ReactNode } from "react";
import "./Window.css";

type WindowProps = {
  title: string;
  onClose: () => void;
  className?: string;
  style?: CSSProperties;
  children: ReactNode;
};

export default function Window({ title, onClose, className, style, children }: WindowProps) {
  return (
    <div className={`Window ${className}`} style={style}>
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
  );
}
