import Alert from "./Alert";
import { useReadPosts } from "./postHooks";

export default function Posts() {
  const postsReading = useReadPosts();

  if (postsReading.isLoading) return <div>Loading...</div>;
  if (postsReading.isError) return <Alert level="error">An unknown error occurred.</Alert>;
  if (postsReading.data.length === 0) return <div>No posts, yet.</div>;

  return (
    <div>
      {postsReading.data.map((post) => (
        <div key={post.id}>
          @{post.username} posted on {post.createdAt.toISOString()}
          <br />
          <img src={post.imageUrl} />
          {post.message && <p>{post.message}</p>}
        </div>
      ))}
    </div>
  );
}
