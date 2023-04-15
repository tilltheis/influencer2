import { useMutation } from "@tanstack/react-query";
import { Session } from "./model";

type SessionResult = { type: "session" } & Session;
type InvalidCredentialsResult = { type: "invalidCredentials" };
type CreateSessionResult = SessionResult | InvalidCredentialsResult;

export default function useCreateSession() {
  return useMutation(
    async (credentials: { username: string; password: string }): Promise<CreateSessionResult> => {
      const response = await fetch("/api/sessions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
      });

      let json;
      switch (response.status) {
        case 200:
          json = await response.json();
          const token: string = json.token;
          const jwtPayload = token.split(".")[1];
          const decodedPayload = JSON.parse(atob(jwtPayload));
          const expiresAtTimestamp: number = decodedPayload.exp;
          const session: Session = {
            token,
            userId: decodedPayload.id,
            username: decodedPayload.username,
            expiresAtTimestamp,
          };
          return { ...session, type: "session" };
        case 401:
          json = await response.json();
          return { ...json, type: "invalidCredentials" };
        default:
          throw new Error(
            `Create session mutation failed. Unexpected response status ${response.status}.`
          );
      }
    }
  );
}
