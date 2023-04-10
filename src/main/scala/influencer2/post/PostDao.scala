package influencer2.post

import zio.UIO

trait PostDao:
  def createPost(post: Post): UIO[Unit]
