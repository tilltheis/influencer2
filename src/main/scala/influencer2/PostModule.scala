package influencer2

import influencer2.domain.{PostDao, PostService}
import influencer2.infrastructure.MongoPostDao
import mongo4cats.zio.{ZMongoClient, ZMongoDatabase}
import zio.{RLayer, ZLayer}

object PostModule:
  val layer: RLayer[ZMongoClient & ZMongoDatabase, PostService] =
    ZLayer.makeSome[ZMongoClient & ZMongoDatabase, PostService](MongoPostDao.layer, PostService.layer)
