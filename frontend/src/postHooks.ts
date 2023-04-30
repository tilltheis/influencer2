import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchJson } from "./http";
import { PostModel, SessionModel } from "./model";

export type PostResult = { type: "post" } & PostModel;
export type InvalidInputResult = { type: "invalidInput" };
export type CreatePostResult = PostResult | InvalidInputResult;

export function useCreatePost(token: string) {
  const queryClient = useQueryClient();

  return useMutation(
    async (draft: { imageUrl: string; message: string }): Promise<CreatePostResult> => {
      return fetchJson({
        method: "POST",
        url: "/api/posts",
        token,
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
          queryClient.setQueryData<PostModel[]>(["posts"], (existingData) =>
            existingData ? [result, ...existingData] : [result]
          );
      },
    }
  );
}

export function useReadPosts() {
  return useQuery(["posts"], async (): Promise<PostModel[]> => {
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

type PostLiked = { type: "postLiked" };
type PostNotFound = { type: "postNotFound" };
type LikePostResult = PostLiked | PostNotFound;
export function useLikePost({ token, userId, username }: SessionModel) {
  const queryClient = useQueryClient();

  return useMutation(
    async ({ postId }: { postId: string }): Promise<LikePostResult> => {
      return fetchJson<LikePostResult>({
        method: "PUT",
        url: `/api/posts/${postId}/likes/${username}`,
        token,
        responseDataMapper: {
          201: (json: any) => ({ type: "postLiked" }),
          404: (json: any) => ({ type: "postNotFound" }),
        },
      });
    },
    {
      onSuccess: (result, { postId }) => {
        function updatePost(post: PostModel) {
          if (post.id !== postId) return post;
          return { ...post, likes: { ...post.likes, [userId]: username } };
        }

        if (result.type === "postLiked") {
          queryClient.setQueryData<PostModel[]>(
            ["posts"],
            (existingData) => existingData && existingData.map(updatePost)
          );
          queryClient.setQueryData<PostModel>(
            ["posts", postId],
            (existingData) => existingData && updatePost(existingData)
          );
        }
      },
    }
  );
}

type PostUnliked = { type: "postUnliked" };
type UnlikePostResult = PostUnliked | PostNotFound;
export function useUnlikePost({ token, userId, username }: SessionModel) {
  const queryClient = useQueryClient();

  return useMutation(
    async ({ postId }: { postId: string }): Promise<UnlikePostResult> => {
      return fetchJson<UnlikePostResult>({
        method: "DELETE",
        url: `/api/posts/${postId}/likes/${username}`,
        token,
        responseDataMapper: {
          200: (json: any) => ({ type: "postUnliked" }),
          404: (json: any) => ({ type: "postNotFound" }),
        },
      });
    },
    {
      onSuccess: (result, { postId }) => {
        function updatePost(post: PostModel) {
          if (post.id !== postId) return post;
          const { [userId]: _, ...likes } = post.likes;
          return { ...post, likes };
        }

        if (result.type === "postUnliked") {
          queryClient.setQueryData<PostModel[]>(
            ["posts"],
            (existingData) => existingData && existingData.map(updatePost)
          );
          queryClient.setQueryData<PostModel>(
            ["posts", postId],
            (existingData) => existingData && updatePost(existingData)
          );
        }
      },
    }
  );
}
