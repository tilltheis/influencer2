export type Session = {
  token: string;
  userId: string;
  username: string;
  expiresAtTimestamp: number;
};

export type User = { id: string; username: string };
