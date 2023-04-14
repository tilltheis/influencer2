import "react";
import Window from "./Window";

type RegisterWindowProps = {
  close: () => void;
  showLoginWindow: () => void;
};

export default function RegisterWindow({
  close,
  showLoginWindow,
}: RegisterWindowProps) {
  return (
    <Window title="Create Account" className="RegisterWindow" close={close}>
      <label>
        Username: <input type="text" name="username" />
      </label>
      <label>
        Password: <input type="password" name="password" />
      </label>
      <button>Create account</button>
      or
      <button className="Button--asLink" onClick={showLoginWindow}>
        Login with existing user
      </button>
    </Window>
  );
}
