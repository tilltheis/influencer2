import "react";
import { FormEvent } from "react";
import "./LogoutWindow.css";
import { SessionModel } from "./model";
import { useDeleteSession } from "./sessionHooks";
import Window from "./Window";

type LogoutWindowProps = {
  session: SessionModel;
  onClose: () => void;
  onLogout: () => void;
};

export default function LogoutWindow({ session, onClose, onLogout }: LogoutWindowProps) {
  const sessionDeletion = useDeleteSession(session.token);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    sessionDeletion.mutate(undefined, { onSuccess: () => onLogout() });
  }

  return (
    <Window title={session.username} className="LogoutWindow" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {sessionDeletion.isError && "An unknown error occurred."}
        <button>Logout</button>
      </form>
    </Window>
  );
}
