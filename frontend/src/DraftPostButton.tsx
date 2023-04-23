import { faPenToSquare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import DraftPostWindow from "./DraftPostWindow";

export default function DraftPostButton() {
  const [showWindow, setShowWindow] = useState(false);

  const window = showWindow ? (
    <DraftPostWindow onPost={() => setShowWindow(false)} onClose={() => setShowWindow(false)} />
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
        <FontAwesomeIcon icon={faPenToSquare} /> post
      </button>
      {window}
    </div>
  );
}
