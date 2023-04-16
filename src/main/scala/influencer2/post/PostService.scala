package influencer2.post

import influencer2.user.UserId
import zio.{Clock, IO, UIO, URLayer, ZLayer}

class PostService(postDao: PostDao):
  def createPost(userId: UserId, username: String, imageUrl: HttpsUrl, message: Option[String]): UIO[Post] =
    for
      postId    <- PostId.random
      createdAt <- Clock.instant
      post = Post(postId, userId, username, createdAt, imageUrl, message)
      _ <- postDao.createPost(post)
    yield post
end PostService

object PostService:
  val layer: URLayer[PostDao, PostService] = ZLayer.fromFunction(PostService(_))
