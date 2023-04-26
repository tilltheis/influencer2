package influencer2

import influencer2.post.{MongoPostDao, PostDao, PostService}
import mongo4cats.zio.{ZMongoClient, ZMongoDatabase}
import zio.{RLayer, ZLayer}

object PostModule:
  val layer: RLayer[ZMongoClient & ZMongoDatabase, PostService] =
    ZLayer.makeSome[ZMongoClient & ZMongoDatabase, PostService](MongoPostDao.layer, PostService.layer)
