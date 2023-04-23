package influencer2.post

import influencer2.user.{UserDao, UserId}
import zio.{Clock, IO, UIO, URLayer, ZIO, ZLayer}

class PostService(postDao: PostDao, userDao: UserDao):
  // TODO: use transaction
  def createPost(userId: UserId, username: String, imageUrl: HttpsUrl, message: Option[String]): UIO[Post] =
    for
      // user id has already been verified, no need to double check it, it's ok to crash
      _         <- userDao.incrementPostCount(userId).orDieWith(_.toException)
      postId    <- PostId.random
      createdAt <- Clock.instant
      post = Post(postId, userId, username, createdAt, imageUrl, message)
      _ <- postDao.createPost(post)
    yield post

  val readPosts: UIO[Seq[Post]] = postDao.loadPosts
end PostService

object PostService:
  val layer: URLayer[PostDao & UserDao, PostService] = ZLayer {
    for
      postDao <- ZIO.service[PostDao]
      userDao <- ZIO.service[UserDao]
    yield PostService(postDao, userDao)
  }
