import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "react";

type WindowProps = {
  title: string;
  close: () => void;
  className?: string;
  children: React.ReactNode;
};

export default function Window({
  title,
  close,
  className,
  children,
}: WindowProps) {
  return (
    <div className={`Window ${className}`}>
      <h3>
        {title}
        <FontAwesomeIcon
          icon={faXmark}
          title="Close"
          role="button"
          cursor="pointer"
          onClick={close}
          className="Window__CloseButton"
        />
      </h3>
      <div className="Window__Children">{children}</div>
    </div>
  );
}
