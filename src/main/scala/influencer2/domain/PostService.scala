package influencer2.domain

import zio.{Clock, IO, UIO, URLayer, ZIO, ZLayer}

class PostService(postDao: PostDao):
  def createPost(userId: UserId, username: String, imageUrl: HttpsUrl, message: Option[String]): UIO[Post] =
    for
      postId    <- PostId.random
      createdAt <- Clock.instant
      post = Post(postId, userId, username, createdAt, imageUrl, message, Map.empty)
      _ <- postDao.createPost(post)
    yield post

  val readAllPosts: UIO[Seq[Post]] = postDao.loadAllPosts

  def readUserPosts(username: String): UIO[Seq[Post]] = postDao.loadUserPosts(username)

  def readPost(postId: PostId): IO[PostNotFound.type, Post] = postDao.loadPost(postId)

  def likePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type | PostAlreadyLiked.type, Unit] =
    postDao.likePost(userId, username, postId)

  def unlikePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type, Unit] =
    postDao.unlikePost(userId, username, postId)
end PostService

object PostService:
  val layer: URLayer[PostDao, PostService] = ZLayer.fromFunction(PostService(_))
