import { ChangeEvent, FormEvent, SyntheticEvent, useState } from "react";
import Alert from "./Alert";
import "./DraftPostWindow.css";
import Input from "./Input";
import { PostModel, SessionModel } from "./model";
import { useCreatePost } from "./postHooks";
import { useExistingSession } from "./SessionContext";
import Window from "./Window";

export type DraftPostWindowProps = {
  onClose: () => void;
  onPost: (post: PostModel) => void;
};

export default function DraftPostWindow({ onClose, onPost }: DraftPostWindowProps) {
  const { session } = useExistingSession();

  const [imageUrl, setImageUrl] = useState("");
  const [message, setMessage] = useState("");

  const [isValidImageUrl, setValidImageUrl] = useState(true);
  const [lastLoadedImageUrl, setLastLoadedImageUrl] = useState<string | null>(null);

  const sessionCreation = useCreatePost(session.token);

  const httpsRegex = new RegExp("^https://", "i");

  function handleImageUrlChanged(e: ChangeEvent<HTMLInputElement>) {
    const url = e.target.value;
    setImageUrl(url);
    setValidImageUrl(httpsRegex.test(url) || url === "");
  }

  function handleFormSubmitted(e: FormEvent) {
    e.preventDefault();
    sessionCreation.mutate(
      { imageUrl, message },
      { onSuccess: (result) => result.type == "post" && onPost(result) }
    );
  }

  function handlePreviewImageLoaded(e: SyntheticEvent<HTMLImageElement, Event>) {
    if (e.currentTarget.complete && e.currentTarget.naturalWidth !== 0)
      setLastLoadedImageUrl(e.currentTarget.src);
  }

  return (
    <Window className="DraftPostWindow" title="Draft Post" onClose={onClose}>
      <form onSubmit={handleFormSubmitted}>
        <div className="DraftPostWindow__ImagePreview">
          <div className="DraftPostWindow__ImagePreviewText">
            <h3>Image Preview</h3>
            <p>Please enter image URL below.</p>
          </div>
          <img
            className="DraftPostWindow__ImagePreviewImage"
            src={imageUrl}
            onLoad={handlePreviewImageLoaded}
          />
        </div>
        {!isValidImageUrl && <Alert level="error">Image URL must start with "https://".</Alert>}
        {sessionCreation.isError && <Alert level="error">An unknown error occurred.</Alert>}
        {sessionCreation.data?.type == "invalidInput" && (
          <Alert level="error">Post data validation failed. Please check your input.</Alert>
        )}
        <Input
          label="Image URL"
          type="url"
          pattern="^[hH][tT][tT][pP][sS]://.+"
          placeholder="https://"
          onChange={handleImageUrlChanged}
          required
        />
        <Input multiline label="Message" onChange={(e) => setMessage(e.target.value)} />
        {lastLoadedImageUrl == imageUrl ? (
          <button className="button--asButton">Publish post</button>
        ) : (
          <button className="button--asButton" disabled>
            Publish post (invalid URL or image still loading)
          </button>
        )}
      </form>
    </Window>
  );
}
