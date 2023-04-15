import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./Window.css";

type WindowProps = {
  title: string;
  onClose: () => void;
  className?: string;
  children: React.ReactNode;
};

export default function Window({ title, onClose, className, children }: WindowProps) {
  return (
    <div className={`Window ${className}`}>
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
