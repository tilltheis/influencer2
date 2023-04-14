import "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRightToBracket,
  faUser,
  faXmark,
} from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";
import LoginWindow from "./LoginWindow";
import RegisterWindow from "./RegisterWindow";

type LoginButtonProps = {
  showLoginWindow: () => void;
};
function LoginButton({ showLoginWindow }: LoginButtonProps) {
  return (
    <FontAwesomeIcon
      icon={faArrowRightToBracket}
      title="Login"
      role="button"
      cursor="pointer"
      onClick={showLoginWindow}
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
  const [formStatus, setWindowStatus] = useState<
    "showNoWindow" | "showLoginWindow" | "showRegisterWindow"
  >("showNoWindow");

  let ButtonEl;
  switch (buttonStatus) {
    case "showLoginButton":
      ButtonEl = () => (
        <LoginButton
          showLoginWindow={() => setWindowStatus("showLoginWindow")}
        />
      );
      break;
    case "showProfileButton":
      ButtonEl = () => <ProfileButton />;
      break;
  }

  let WindowEl;
  switch (formStatus) {
    case "showLoginWindow":
      WindowEl = () => (
        <LoginWindow
          close={() => setWindowStatus("showNoWindow")}
          showRegisterWindow={() => setWindowStatus("showRegisterWindow")}
        />
      );
      break;
    case "showRegisterWindow":
      WindowEl = () => (
        <RegisterWindow
          close={() => setWindowStatus("showNoWindow")}
          showLoginWindow={() => setWindowStatus("showLoginWindow")}
        />
      );
      break;
    case "showNoWindow":
      WindowEl = () => <></>;
      break;
  }

  return (
    <div className="LoginStatus">
      <ButtonEl />
      <WindowEl />
    </div>
  );
}
