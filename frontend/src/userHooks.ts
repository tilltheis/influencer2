import { useMutation } from "@tanstack/react-query";
import { User } from "./model";

export type UserResult = { type: "user" } & User;
export type UsernameUnavailableResult = { type: "usernameUnavailable" };
export type CreateUserResult = UserResult | UsernameUnavailableResult;

export function useCreateUser() {
  return useMutation(
    async (credentials: { username: string; password: string }): Promise<CreateUserResult> => {
      const { username, ...data } = credentials;
      const response = await fetch(`/api/users/${encodeURIComponent(username)}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      let json;
      switch (response.status) {
        case 201:
          json = await response.json();
          return { ...json, type: "user" };
        case 409:
          json = await response.json();
          return { ...json, type: "usernameUnavailable" };
        default:
          throw new Error(
            `Create user mutation failed. Unexpected response status ${response.status}.`
          );
      }
    }
  );
}
