import "react";
import { ChangeEvent, FormEvent, MouseEvent, useState } from "react";
import Alert from "./Alert";
import Input from "./Input";
import { useCreateUser } from "./userHooks";

type RegisterWindowContentProps = {
  onShowLoginWindow: () => void;
};

export default function RegisterWindowContent({ onShowLoginWindow }: RegisterWindowContentProps) {
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
      <Input label="Username" onChange={handleUsernameChanged} autoFocus />
      <Input label="Password" type="password" onChange={handlePasswordChanged} />
      <button className="button--asButton">Create account</button>
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

  return userCreation.data?.type == "user" ? successMessage : form;
}
