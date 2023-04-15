import { useMutation } from "@tanstack/react-query";
import { Session } from "./model";

export type SessionResult = { type: "session" } & Session;
export type InvalidCredentialsResult = { type: "invalidCredentials" };
export type CreateSessionResult = SessionResult | InvalidCredentialsResult;

export function useCreateSession() {
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

export function useDeleteSession(token: string) {
  return useMutation(async (): Promise<void> => {
    const response = await fetch("/api/sessions", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    switch (response.status) {
      case 200:
        return;
      default:
        throw new Error(
          `Delete session mutation failed. Unexpected response status ${response.status}.`
        );
    }
  });
}
