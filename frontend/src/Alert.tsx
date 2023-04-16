import { CSSProperties, ReactNode } from "react";
import "./Alert.css";

type AlertProps = {
  level: "error" | "success";
  children: ReactNode;
  style?: CSSProperties;
};

export default function Alert({ level, style, children }: AlertProps) {
  return (
    <div className={`Alert Alert--${level}`} style={style}>
      {children}
    </div>
  );
}
