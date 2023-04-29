export type SessionModel = {
  token: string;
  userId: string;
  username: string;
  expiresAtTimestamp: number;
};

export type UserModel = {
  id: string;
  username: string;
  postCount: number;
  followerCount: number;
  followeeCount: number;
};

export type PostModel = {
  id: string;
  userId: string;
  username: string;
  createdAt: Date;
  imageUrl: string;
  message: string | null;
};
