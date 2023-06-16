import { faHeart as faHeartSolid } from "@fortawesome/free-solid-svg-icons";
import { faHeart as faHeartRegular } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import { PostModel, SessionModel } from "./model";
import "./Post.css";
import UserPreview from "./UserPreview";
import { useLikePost, useUnlikePost } from "./postHooks";
import { useSession } from "./SessionContext";

export type PostProps = PostModel & {};
export default function Post({
  id,
  userId,
  username,
  createdAt,
  imageUrl,
  message,
  likes,
}: PostProps) {
  const [isUserPreviewVisible, setUserPreviewVisible] = useState(false);
  const userPreview = isUserPreviewVisible ? <UserPreview tooltip username={username} /> : null;

  const { session } = useSession();

  function EnabledLikeButton({ session }: { session: SessionModel }) {
    const postLiking = useLikePost(session);
    const postUnliking = useUnlikePost(session);
    const isLiked = !!(session && likes[session.userId]);

    function handleLikeButtonClicked() {
      if (isLiked) postUnliking.mutate({ postId: id });
      else postLiking.mutate({ postId: id });
    }

    return (
      <button onClick={handleLikeButtonClicked}>
        <FontAwesomeIcon icon={isLiked ? faHeartSolid : faHeartRegular} />{" "}
        {Object.keys(likes).length}
      </button>
    );
  }

  function DisabledLikeButton() {
    return (
      <button disabled>
        <FontAwesomeIcon icon={faHeartRegular} /> {Object.keys(likes).length}
      </button>
    );
  }

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
      {session ? <EnabledLikeButton session={session} /> : <DisabledLikeButton />}
    </div>
  );
}
