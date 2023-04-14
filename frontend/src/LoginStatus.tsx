import "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRightToBracket,
  faUser,
  faXmark,
} from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";

type LoginFormProps = {
  login: () => void;
  close: () => void;
  showRegisterForm: () => void;
};

function LoginForm({ login, close, showRegisterForm }: LoginFormProps) {
  return (
    <div className="LoginStatus__Form">
      <h3>
        Login
        <FontAwesomeIcon
          icon={faXmark}
          title="Close"
          role="button"
          cursor="pointer"
          onClick={close}
          className="LoginStatus__Form__CloseButton"
        />
      </h3>
      <label>
        Username: <input type="text" name="username" />
      </label>
      <label>
        Password: <input type="password" name="password" />
      </label>
      <button onClick={login}>Login</button>
      or
      <button className="Button--asLink" onClick={showRegisterForm}>
        Create new account
      </button>
    </div>
  );
}

type RegisterFormProps = {
  close: () => void;
  showLoginForm: () => void;
};
function RegisterForm({ close, showLoginForm }: RegisterFormProps) {
  return (
    <div className="LoginStatus__Form">
      <h3>
        Create Account
        <FontAwesomeIcon
          icon={faXmark}
          title="Close"
          role="button"
          cursor="pointer"
          onClick={close}
          className="LoginStatus__Form__CloseButton"
        />
      </h3>
      <label>
        Username: <input type="text" name="username" />
      </label>
      <label>
        Password: <input type="password" name="password" />
      </label>
      <button>Create account</button>
      or
      <button className="Button--asLink" onClick={showLoginForm}>
        Login with existing user
      </button>
    </div>
  );
}

type LoginButtonProps = {
  showLoginForm: () => void;
};
function LoginButton({ showLoginForm }: LoginButtonProps) {
  return (
    <FontAwesomeIcon
      icon={faArrowRightToBracket}
      title="Login"
      role="button"
      cursor="pointer"
      onClick={showLoginForm}
    />
  );
}

function ProfileButton() {
  return (
    <FontAwesomeIcon
      icon={faUser}
      title="Profile"
      role="button"
      cursor="pointer"
    />
  );
}

export default function LoginStatus() {
  const [buttonStatus, setButtonStatus] = useState<
    "showLoginButton" | "showProfileButton"
  >("showLoginButton");
  const [formStatus, setFormStatus] = useState<
    "showNoForm" | "showLoginForm" | "showRegisterForm"
  >("showNoForm");

  let ButtonEl;
  switch (buttonStatus) {
    case "showLoginButton":
      ButtonEl = () => (
        <LoginButton showLoginForm={() => setFormStatus("showLoginForm")} />
      );
      break;
    case "showProfileButton":
      ButtonEl = () => <ProfileButton />;
      break;
  }

  let FormEl;
  switch (formStatus) {
    case "showLoginForm":
      FormEl = () => (
        <LoginForm
          login={() => {
            setButtonStatus("showProfileButton");
            setFormStatus("showNoForm");
          }}
          close={() => setFormStatus("showNoForm")}
          showRegisterForm={() => setFormStatus("showRegisterForm")}
        />
      );
      break;
    case "showRegisterForm":
      FormEl = () => (
        <RegisterForm
          close={() => setFormStatus("showNoForm")}
          showLoginForm={() => setFormStatus("showLoginForm")}
        />
      );
      break;
    case "showNoForm":
      FormEl = () => <></>;
      break;
  }

  return (
    <div className="LoginStatus">
      <ButtonEl />
      <FormEl />
    </div>
  );
}
