import { faArrowRightToBracket } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import LoginRegisterWindow from "./LoginRegisterWindow";

export default function LoginRegisterButton() {
  const [showWindow, setShowWindow] = useState(false);

  return (
    <div className="LoginRegisterButton">
      <button className="button--asText" onClick={() => setShowWindow(true)}>
        <FontAwesomeIcon icon={faArrowRightToBracket} /> login
      </button>
      {showWindow && <LoginRegisterWindow onClose={() => setShowWindow(false)} />}
    </div>
  );
}
