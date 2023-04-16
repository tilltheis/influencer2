import "react";
import { ChangeEvent, FormEvent, MouseEvent, useState } from "react";
import Alert from "./Alert";
import "./RegisterWindow.css";
import { useCreateUser } from "./userHooks";
import Window from "./Window";

type RegisterWindowProps = {
  onClose: () => void;
  onShowLoginWindow: () => void;
};

export default function RegisterWindow({ onClose, onShowLoginWindow }: RegisterWindowProps) {
  const userCreation = useCreateUser();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function handleFormSubmitted(e: FormEvent) {
    e.preventDefault();
    userCreation.mutate({ username, password });
  }

  const handleUsernameChanged = (e: ChangeEvent<HTMLInputElement>) => setUsername(e.target.value);
  const handlePasswordChanged = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);
  const handleShowLoginWindowClicked = (e: MouseEvent) => {
    e.preventDefault();
    onShowLoginWindow();
  };

  const form = (
    <form onSubmit={handleFormSubmitted}>
      {userCreation.isError && <Alert level="error">An unknown error occurred.</Alert>}
      {userCreation.data?.type == "usernameUnavailable" && (
        <Alert level="error">Username unavailable.</Alert>
      )}
      <label>
        Username: <input type="text" name="username" onChange={handleUsernameChanged} autoFocus />
      </label>
      <label>
        Password: <input type="password" name="password" onChange={handlePasswordChanged} />
      </label>
      <button>Create account</button>
      or
      <button className="button--asLink" onClick={handleShowLoginWindowClicked}>
        Login with existing user
      </button>
    </form>
  );

  const successMessage = (
    <>
      <Alert level="success">New user account created for {username}!</Alert>
      <button className="button--asLink" onClick={handleShowLoginWindowClicked}>
        Login with existing user
      </button>
    </>
  );

  return (
    <Window title="Create Account" className="RegisterWindow" onClose={onClose}>
      {userCreation.data?.type == "user" ? successMessage : form}
    </Window>
  );
}
