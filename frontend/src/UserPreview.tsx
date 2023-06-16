import { Link } from "react-router-dom";
import Alert from "./Alert";
import { useReadUser } from "./userHooks";
import "./UserPreview.css";
import classNames from "./classNames";

export type UserPreviewProps = { username: string; tooltip?: boolean };
export default function UserPreview({ username, tooltip }: UserPreviewProps) {
  const { isLoading, isError, data: user } = useReadUser(username);

  if (isLoading) return <></>;

  let content;
  if (isError) content = <Alert level="error">An unknown error occurred.</Alert>;
  else if (user === null) content = <Alert level="info">User not found.</Alert>;
  else
    content = (
      <dl>
        <dt>Registered</dt>
        <dd>{user.createdAt.toISOString()}</dd>
        <dt>Posts</dt>
        <dd>{user.postCount}</dd>
        <dt>Followers</dt>
        <dd>{user.followerCount}</dd>
        <dt>Followees</dt>
        <dd>{user.followeeCount}</dd>
      </dl>
    );

  return (
    <div
      className={classNames("UserPreview", tooltip && "UserPreview--tooltip")}
      onClick={(e) => e.stopPropagation()}
    >
      <h3>
        <Link to={`/${username}`}>{username}</Link>
      </h3>
      {content}
    </div>
  );
}
