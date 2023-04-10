package influencer2.post

import zio.{URLayer, ZLayer}

class PostService(postDao: PostDao)

object PostService:
  val layer: URLayer[PostDao, PostService] = ZLayer.fromFunction(PostService(_))
