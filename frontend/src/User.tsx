import UserPreview from "./UserPreview";
import Posts from "./Posts";

export type UserProps = { username: string };
export default function User({ username }: UserProps) {
  return (
    <>
      <UserPreview username={username} />
      <br />
      <Posts username={username} />
    </>
  );
}
