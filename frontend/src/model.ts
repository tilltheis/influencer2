export type Session = {
  token: string;
  userId: string;
  username: string;
  expiresAtTimestamp: number;
};

export type User = { id: string; username: string };

export type Post = {
  id: string;
  userId: string;
  username: string;
  createdAt: Date;
  imageUrl: string;
  message: string | null;
};
