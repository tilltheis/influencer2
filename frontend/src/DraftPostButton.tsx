import { faPenToSquare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import "./DraftPostButton.css";
import DraftPostWindow from "./DraftPostWindow";
import { Session } from "./model";

export type DraftPostButtonProps = {
  session: Session;
};
export default function DraftPostButton({ session }: DraftPostButtonProps) {
  const [showWindow, setShowWindow] = useState(false);

  const window = showWindow ? (
    <DraftPostWindow session={session} onClose={() => setShowWindow(false)} />
  ) : (
    <></>
  );

  return (
    <div className="DraftPostButton">
      <button
        className="button--asText"
        onClick={() => setShowWindow(true)}
        style={{ marginRight: "1rem" }}
      >
        post <FontAwesomeIcon icon={faPenToSquare} />
      </button>
      {window}
    </div>
  );
}
