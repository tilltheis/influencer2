package influencer2.domain

import influencer2.domain.PostNotFound
import zio.{IO, UIO}

trait PostDao:
  def createPost(post: Post): UIO[Unit]
  val loadPosts: UIO[Seq[Post]]
  def loadPost(postId: PostId): IO[PostNotFound.type, Post]
  def likePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type | PostAlreadyLiked.type, Unit]
  def unlikePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type | PostNotLiked.type, Unit]
