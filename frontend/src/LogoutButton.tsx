import { faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import "./LogoutButton.css";
import LogoutWindow from "./LogoutWindow";
import { useExistingSession } from "./SessionContext";

export default function LogoutButton() {
  const { session, setSession } = useExistingSession();
  const [showWindow, setShowWindow] = useState(false);

  return (
    <div className="LogoutButton">
      <button className="button--asText" onClick={() => setShowWindow(true)}>
        {session.username} <FontAwesomeIcon icon={faUser} title="Profile" />
      </button>
      {showWindow && (
        <LogoutWindow
          session={session}
          onClose={() => setShowWindow(false)}
          onLogout={() => setSession(null)}
        />
      )}
    </div>
  );
}
