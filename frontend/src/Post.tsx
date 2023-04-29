import { useState } from "react";
import { PostModel } from "./model";
import "./Post.css";
import UserPreview from "./UserPreview";

export type PostProps = PostModel & {};

export default function Post({ id, userId, username, createdAt, imageUrl, message }: PostProps) {
  const [isUserPreviewVisible, setUserPreviewVisible] = useState(false);
  const userPreview = isUserPreviewVisible ? <UserPreview username={username} /> : null;

  return (
    <div className="Post">
      <h3 className="Post__Title">
        <span
          tabIndex={0}
          className="button--asLink"
          onMouseOver={() => setUserPreviewVisible(true)}
          onMouseOut={() => setUserPreviewVisible(false)}
          onFocus={() => setUserPreviewVisible(true)}
          onBlur={() => setUserPreviewVisible(false)}
          onClick={() => setUserPreviewVisible(!isUserPreviewVisible)}
        >
          @{username}
          {userPreview}
        </span>{" "}
        posted on {createdAt.toISOString()}
      </h3>
      <img className="Post__Image" src={imageUrl} />
      <p className="Post__Message">{message}</p>
    </div>
  );
}
