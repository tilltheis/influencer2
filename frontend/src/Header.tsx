import { Link } from "react-router-dom";
import DraftPostButton from "./DraftPostButton";
import "./Header.css";
import LoginRegisterButton from "./LoginRegisterButton";
import LogoutButton from "./LogoutButton";
import { useSession } from "./SessionContext";

export default function Header() {
  const { session } = useSession();

  let headerButtons;

  if (session) {
    headerButtons = (
      <>
        <DraftPostButton />
        <LogoutButton />
      </>
    );
  } else {
    headerButtons = (
      <>
        <LoginRegisterButton />
      </>
    );
  }

  return (
    <header className="Header">
      <div className="Header__Content">
        <h1>
          <Link to="/">Influencer&nbsp;2</Link>
        </h1>
        {headerButtons}
      </div>
    </header>
  );
}
