import { useMutation } from "@tanstack/react-query";
import { fetchJson } from "./http";
import { SessionModel } from "./model";

export type SessionResult = { type: "session" } & SessionModel;
export type InvalidCredentialsResult = { type: "invalidCredentials" };
export type CreateSessionResult = SessionResult | InvalidCredentialsResult;

export function useCreateSession() {
  return useMutation(
    async (credentials: { username: string; password: string }): Promise<CreateSessionResult> =>
      fetchJson({
        method: "POST",
        url: "/api/sessions",
        requestData: credentials,
        responseDataMapper: {
          200: (json) => {
            const token: string = json.token;
            const jwtPayload = token.split(".")[1];
            const decodedPayload = JSON.parse(atob(jwtPayload));
            const expiresAtTimestamp: number = decodedPayload.exp;
            const session: SessionModel = {
              token,
              userId: decodedPayload.id,
              username: decodedPayload.username,
              expiresAtTimestamp,
            };
            return { ...session, type: "session" };
          },
          401: (json) => ({ ...json, type: "invalidCredentials" }),
        },
      })
  );
}

export function useDeleteSession(authToken: string) {
  return useMutation(
    async (): Promise<void> =>
      fetchJson({
        method: "DELETE",
        url: "/api/sessions",
        authToken,
        responseDataMapper: {
          200: () => {},
        },
      })
  );
}
