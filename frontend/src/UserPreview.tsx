import Alert from "./Alert";
import { useReadUser } from "./userHooks";
import "./UserPreview.css";

export type UserPreviewProps = { username: string };
export default function UserPreview({ username }: UserPreviewProps) {
  const { isLoading, isError, data: user } = useReadUser(username);

  if (isLoading) return <></>;

  let content;
  if (isError) content = <Alert level="error">An unknown error occurred.</Alert>;
  else
    content = (
      <dl>
        <dt>Posts</dt>
        <dd>{user.postCount}</dd>
        <dt>Followers</dt>
        <dd>{user.followerCount}</dd>
        <dt>Followees</dt>
        <dd>{user.followeeCount}</dd>
      </dl>
    );

  return (
    <div className="UserPreview" onClick={(e) => e.stopPropagation()}>
      <h3>{username}</h3>
      {content}
    </div>
  );
}
