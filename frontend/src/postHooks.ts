import { useMutation } from "@tanstack/react-query";
import { fetchJson } from "./http";
import { Post } from "./model";

export type PostResult = { type: "post" } & Post;
export type InvalidInputResult = { type: "invalidInput" };
export type CreatePostResult = PostResult | InvalidInputResult;

export function useCreatePost(authToken: string) {
  return useMutation(
    async (draft: { imageUrl: string; message: string }): Promise<CreatePostResult> => {
      return fetchJson({
        method: "POST",
        url: "/api/posts",
        authToken,
        requestData: draft,
        responseDataMapper: {
          201: (json: any) => ({ ...json, type: "post" }),
          422: (json: any) => ({ ...json, type: "invalidInput" }),
        },
      });
    }
  );
}
