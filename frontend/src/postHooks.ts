import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchJson } from "./http";
import { Post } from "./model";

export type PostResult = { type: "post" } & Post;
export type InvalidInputResult = { type: "invalidInput" };
export type CreatePostResult = PostResult | InvalidInputResult;

export function useCreatePost(authToken: string) {
  const queryClient = useQueryClient();

  return useMutation(
    async (draft: { imageUrl: string; message: string }): Promise<CreatePostResult> => {
      return fetchJson({
        method: "POST",
        url: "/api/posts",
        authToken,
        requestData: draft,
        responseDataMapper: {
          201: (json: any) => ({ ...json, createdAt: new Date(json.createdAt), type: "post" }),
          422: (json: any) => ({ ...json, type: "invalidInput" }),
        },
      });
    },
    {
      onSuccess: (result) => {
        if (result.type === "post")
          queryClient.setQueryData<Post[]>(["posts"], (existingData) =>
            existingData ? [result, ...existingData] : [result]
          );
      },
    }
  );
}

export function useReadPosts() {
  return useQuery(["posts"], async (): Promise<Post[]> => {
    return fetchJson({
      method: "GET",
      url: "/api/posts",
      responseDataMapper: {
        200: (json: any[]) =>
          json.map((post) => ({ ...post, createdAt: new Date(post.createdAt) })),
      },
    });
  });
}
