package influencer2.domain

import zio.UIO

trait PostDao:
  def createPost(post: Post): UIO[Unit]
  val loadPosts: UIO[Seq[Post]]
