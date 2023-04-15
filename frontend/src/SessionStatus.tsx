import "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faArrowRightToBracket, faUser, faXmark } from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";
import LoginWindow from "./LoginWindow";
import RegisterWindow from "./RegisterWindow";
import { Session } from "./model";
import "./SessionStatus.css";
import LogoutWindow from "./LogoutWindow";

type LoggedInStatusProps = { session: Session; onLogout: () => void };
const LoggedInStatus = ({ session, onLogout }: LoggedInStatusProps) => {
  const [showWindow, setShowWindow] = useState(false);

  return (
    <>
      <button onClick={() => setShowWindow(true)}>
        {session.username} <FontAwesomeIcon icon={faUser} title="Profile" />
      </button>
      {showWindow && (
        <LogoutWindow session={session} onClose={() => setShowWindow(false)} onLogout={onLogout} />
      )}
    </>
  );
};

type LoggedOutStatusProps = { onLogin: (session: Session) => void };
const LoggedOutStatus = ({ onLogin }: LoggedOutStatusProps) => {
  const [windowStatus, setWindowStatus] = useState<
    "showNoWindow" | "showLoginWindow" | "showRegisterWindow"
  >("showNoWindow");

  let WindowEl;
  switch (windowStatus) {
    case "showLoginWindow":
      WindowEl = () => (
        <LoginWindow
          onClose={() => setWindowStatus("showNoWindow")}
          onLogin={(session) => {
            onLogin(session);
            setWindowStatus("showNoWindow");
          }}
          onShowRegisterWindow={() => setWindowStatus("showRegisterWindow")}
        />
      );
      break;
    case "showRegisterWindow":
      WindowEl = () => (
        <RegisterWindow
          onClose={() => setWindowStatus("showNoWindow")}
          onShowLoginWindow={() => setWindowStatus("showLoginWindow")}
        />
      );
      break;
    case "showNoWindow":
      WindowEl = () => <></>;
      break;
  }

  return (
    <>
      <button onClick={() => setWindowStatus("showLoginWindow")}>
        login <FontAwesomeIcon icon={faArrowRightToBracket} />
      </button>
      <WindowEl />
    </>
  );
};

type SessionStatusProps = {
  session: Session | null;
  onLogin: (session: Session) => void;
  onLogout: () => void;
};
export default ({ session, onLogin, onLogout }: SessionStatusProps) => {
  return (
    <div className="SessionStatus">
      {session ? (
        <LoggedInStatus session={session} onLogout={onLogout} />
      ) : (
        <LoggedOutStatus onLogin={onLogin} />
      )}
    </div>
  );
};
