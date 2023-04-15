import { useEffect, useState } from "react";
import { Session } from "./model";

export type UseSessionResult = {
  session: Session | null;
  setSession: (session: Session) => void;
};
export default (): UseSessionResult => {
  const [session, setSession] = useState<Session | null>(() => {
    const sessionString = localStorage.getItem("session");
    return sessionString ? JSON.parse(sessionString) : null;
  });

  useEffect(() => {
    // This will be called once by every component that called this hook.
    // That's OK, because the action is cheap and idempotent.
    if (session) {
      const timeout = session.expiresAtTimestamp * 1000 - Date.now().valueOf();
      // There's a max delay of ~25d in most browsers.
      // See https://developer.mozilla.org/en-US/docs/Web/API/setTimeout#maximum_delay_value.
      // Therefore don't schedule the timeout for big delays. It should be safe to assume that no
      // one keeps the website open for 25d w/o reloading it once.
      const maxTimeout = Math.pow(2, 31) - 1;
      if (timeout <= maxTimeout) {
        const id = setTimeout(() => handleSessionChanged(null), timeout);
        return () => clearTimeout(id);
      }
    }
  }, [session]);

  const handleSessionChanged = (newSession: Session | null) => {
    if (newSession) localStorage.setItem("session", JSON.stringify(newSession));
    else localStorage.removeItem("session");
    setSession(newSession);
  };

  return {
    session,
    setSession: handleSessionChanged,
  };
};
