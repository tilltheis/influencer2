import { faArrowRightToBracket } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import "./LoginRegisterButton.css";
import LoginWindow from "./LoginWindow";
import RegisterWindow from "./RegisterWindow";

export default function LoginRegisterButton() {
  const [windowStatus, setWindowStatus] = useState<
    "showNoWindow" | "showLoginWindow" | "showRegisterWindow"
  >("showNoWindow");

  let window;
  switch (windowStatus) {
    case "showLoginWindow":
      window = (
        <LoginWindow
          onClose={() => setWindowStatus("showNoWindow")}
          onShowRegisterWindow={() => setWindowStatus("showRegisterWindow")}
        />
      );
      break;
    case "showRegisterWindow":
      window = (
        <RegisterWindow
          onClose={() => setWindowStatus("showNoWindow")}
          onShowLoginWindow={() => setWindowStatus("showLoginWindow")}
        />
      );
      break;
    case "showNoWindow":
      window = null;
      break;
  }

  return (
    <div className="LoginRegisterButton">
      <button className="button--asText" onClick={() => setWindowStatus("showLoginWindow")}>
        <FontAwesomeIcon icon={faArrowRightToBracket} /> login
      </button>
      {window}
    </div>
  );
}
