import "react";
import { useState } from "react";
import "./LoginRegisterWindow.css";
import LoginWindowContent from "./LoginWindowContent";
import RegisterWindowContent from "./RegisterWindowContent";
import Window from "./Window";

type LoginWindowProps = {
  onClose: () => void;
};

export default function LoginRegisterWindow({ onClose }: LoginWindowProps) {
  const [windowType, setWindowType] = useState<"login" | "register">("login");

  let title, content;
  switch (windowType) {
    case "login":
      title = "Login";
      content = <LoginWindowContent onShowRegisterWindow={() => setWindowType("register")} />;
      break;

    case "register":
      title = "Create Account";
      content = <RegisterWindowContent onShowLoginWindow={() => setWindowType("login")} />;
      break;
  }

  return (
    <Window title={title} className="LoginRegisterWindow" onClose={onClose}>
      {content}
    </Window>
  );
}
