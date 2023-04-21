import { PostModel } from "./model";
import "./Post.css";

export type PostProps = PostModel & {};

export default function Post({ id, userId, username, createdAt, imageUrl, message }: PostProps) {
  return (
    <div className="Post">
      <h3 className="Post__Title">
        @{username} posted on {createdAt.toISOString()}
      </h3>
      <br />
      <img className="Post__Image" src={imageUrl} />
      {message && <p className="Post__Message">{message}</p>}
    </div>
  );
}
