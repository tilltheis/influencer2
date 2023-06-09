import { faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import LogoutWindow from "./LogoutWindow";
import { useExistingSession } from "./SessionContext";

export default function LogoutButton() {
  const { session, setSession } = useExistingSession();
  const [showWindow, setShowWindow] = useState(false);

  return (
    <div className="LogoutButton">
      <button className="button--asText" onClick={() => setShowWindow(true)}>
        <FontAwesomeIcon icon={faUser} title="Profile" /> {session.username}
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
