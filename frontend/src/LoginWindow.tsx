import "react";
import Window from "./Window";

type LoginWindowProps = {
  close: () => void;
  showRegisterWindow: () => void;
};

export default function LoginWindow({
  close,
  showRegisterWindow,
}: LoginWindowProps) {
  function login() {
    close();
  }

  return (
    <Window title="Login" className="LoginWindow" close={close}>
      <label>
        Username: <input type="text" name="username" />
      </label>
      <label>
        Password: <input type="password" name="password" />
      </label>
      <button onClick={login}>Login</button>
      or
      <button className="Button--asLink" onClick={showRegisterWindow}>
        Create new account
      </button>
    </Window>
  );
}
