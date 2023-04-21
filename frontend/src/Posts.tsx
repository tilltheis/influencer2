import Alert from "./Alert";
import Post from "./Post";
import { useReadPosts } from "./postHooks";

export default function Posts() {
  const postsReading = useReadPosts();

  if (postsReading.isLoading) return <div>Loading...</div>;
  if (postsReading.isError) return <Alert level="error">An unknown error occurred.</Alert>;
  if (postsReading.data.length === 0) return <div>No posts, yet.</div>;

  return (
    <div>
      {postsReading.data.map((post) => (
        <Post key={post.id} {...post} />
      ))}
    </div>
  );
}
