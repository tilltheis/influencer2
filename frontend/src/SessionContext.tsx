import { createContext, ReactNode, useContext, useEffect, useState } from "react";
import { SessionModel } from "./model";

export type SessionContext = {
  session: SessionModel | null;
  setSession: (newSession: SessionModel | null) => void;
};

const defaultSetSession = () => {};
export const SessionContext = createContext<SessionContext>({
  session: null,
  setSession: defaultSetSession,
});

export type SessionProviderProps = {
  children: ReactNode;
};

export function SessionProvider({ children }: SessionProviderProps) {
  const [session, setSession] = useState<SessionModel | null>(() => {
    const sessionString = localStorage.getItem("session");
    return sessionString ? JSON.parse(sessionString) : null;
  });

  useEffect(() => {
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

  const handleSessionChanged = (newSession: SessionModel | null) => {
    if (newSession) localStorage.setItem("session", JSON.stringify(newSession));
    else localStorage.removeItem("session");
    setSession(newSession);
  };

  const context = { session, setSession: handleSessionChanged };

  return <SessionContext.Provider value={context}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionContext {
  const context = useContext(SessionContext);
  if (context.setSession === defaultSetSession)
    throw new Error("useSession() called outside of Session context.");
  return context;
}

export type ExistingSessionContext = {
  session: SessionModel;
  setSession: (newSession: SessionModel | null) => void;
};

export function useExistingSession(): ExistingSessionContext {
  const { session, setSession } = useSession();
  if (!session) throw new Error("No existing session found for useExistingSession().");
  return { session, setSession };
}
