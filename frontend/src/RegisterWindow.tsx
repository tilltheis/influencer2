import "react";
import { MouseEvent } from "react";
import "./RegisterWindow.css";
import Window from "./Window";

type RegisterWindowProps = {
  onClose: () => void;
  onShowLoginWindow: () => void;
};

export default function RegisterWindow({ onClose, onShowLoginWindow }: RegisterWindowProps) {
  const handleShowLoginWindowClicked = (e: MouseEvent) => {
    e.preventDefault();
    onShowLoginWindow();
  };

  return (
    <Window title="Create Account" className="RegisterWindow" onClose={onClose}>
      <form onSubmit={(e) => e.preventDefault()}>
        <label>
          Username: <input type="text" name="username" />
        </label>
        <label>
          Password: <input type="password" name="password" />
        </label>
        <button>Create account</button>
        or
        <button className="button--asLink" onClick={handleShowLoginWindowClicked}>
          Login with existing user
        </button>
      </form>
    </Window>
  );
}
