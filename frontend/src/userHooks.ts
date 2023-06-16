import { useMutation, useQuery } from "@tanstack/react-query";
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
          201: (json: any) => ({ ...json, createdAt: new Date(json.createdAt), type: "user" }),
          409: (json: any) => ({ ...json, type: "usernameUnavailable" }),
        },
      });
    }
  );
}

export function useReadUser(username: string) {
  return useQuery(["users", username], async (): Promise<UserModel | null> => {
    return fetchJson({
      method: "GET",
      url: `/api/users/${username}`,
      responseDataMapper: {
        200: (json: any) => ({ ...json, createdAt: new Date(json.createdAt) }),
        404: () => null,
      },
    });
  });
}
