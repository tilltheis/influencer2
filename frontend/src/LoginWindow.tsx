import "react";
import { ChangeEvent, FormEvent, MouseEvent, useState } from "react";
import Alert from "./Alert";
import "./LoginWindow.css";
import { Session } from "./model";
import { useCreateSession } from "./sessionHooks";
import Window from "./Window";

type LoginWindowProps = {
  onClose: () => void;
  onLogin: (session: Session) => void;
  onShowRegisterWindow: () => void;
};

export default function LoginWindow({ onClose, onLogin, onShowRegisterWindow }: LoginWindowProps) {
  const sessionCreation = useCreateSession();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    sessionCreation.mutate(
      { username, password },
      {
        onSuccess: (response) => response.type === "session" && onLogin(response),
      }
    );
  }

  const handleUsernameChanged = (e: ChangeEvent<HTMLInputElement>) => setUsername(e.target.value);
  const handlePasswordChanged = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);
  const handleShowRegisterWindowClicked = (e: MouseEvent) => {
    e.preventDefault();
    onShowRegisterWindow();
  };

  return (
    <Window title="Login" className="LoginWindow" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {sessionCreation.isError && <Alert level="error">An unknown error occurred.</Alert>}
        {sessionCreation.data?.type == "invalidCredentials" && (
          <Alert level="error">Invalid credentials.</Alert>
        )}
        <label>
          Username: <input type="text" name="username" onChange={handleUsernameChanged} autoFocus />
        </label>
        <label>
          Password: <input type="password" name="password" onChange={handlePasswordChanged} />
        </label>
        <button>Login</button>
        or
        <button className="button--asLink" onClick={handleShowRegisterWindowClicked}>
          Create new account
        </button>
      </form>
    </Window>
  );
}
