import "react";
import { ChangeEvent, FormEvent, MouseEvent, useState } from "react";
import Alert from "./Alert";
import Input from "./Input";
import { useSession } from "./SessionContext";
import { useCreateSession } from "./sessionHooks";

type LoginWindowProps = {
  onShowRegisterWindow: () => void;
};

export default function LoginWindowContent({ onShowRegisterWindow }: LoginWindowProps) {
  const { setSession } = useSession();
  const sessionCreation = useCreateSession();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function handleFormSubmitted(e: FormEvent) {
    e.preventDefault();
    sessionCreation.mutate(
      { username, password },
      {
        onSuccess: (response) => response.type === "session" && setSession(response),
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
    <form onSubmit={handleFormSubmitted}>
      {sessionCreation.isError && <Alert level="error">An unknown error occurred.</Alert>}
      {sessionCreation.data?.type == "invalidCredentials" && (
        <Alert level="error">Invalid credentials.</Alert>
      )}
      <Input label="Username" onChange={handleUsernameChanged} autoFocus />
      <Input label="Password" type="password" onChange={handlePasswordChanged} />
      <button className="button--asButton">Login</button>
      or
      <button className="button--asLink" onClick={handleShowRegisterWindowClicked}>
        Create new account
      </button>
    </form>
  );
}
