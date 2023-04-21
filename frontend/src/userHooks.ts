import { useMutation } from "@tanstack/react-query";
import { fetchJson } from "./http";
import { UserModel } from "./model";

export type UserResult = { type: "user" } & UserModel;
export type UsernameUnavailableResult = { type: "usernameUnavailable" };
export type CreateUserResult = UserResult | UsernameUnavailableResult;

export function useCreateUser() {
  return useMutation(
    async (credentials: { username: string; password: string }): Promise<CreateUserResult> => {
      const { username, ...requestData } = credentials;
      return fetchJson({
        method: "PUT",
        url: `/api/users/${encodeURIComponent(username)}`,
        requestData,
        responseDataMapper: {
          201: (json: any) => ({ ...json, type: "user" }),
          409: (json: any) => ({ ...json, type: "usernameUnavailable" }),
        },
      });
    }
  );
}
