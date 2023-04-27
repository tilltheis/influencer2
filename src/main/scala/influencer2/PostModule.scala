package influencer2

import influencer2.domain.{PostDao, PostService}
import influencer2.infrastructure.{AppMongoClient, MongoPostDao}
import zio.{RLayer, ZLayer}

object PostModule:
  val layer: RLayer[AppMongoClient, PostService] =
    ZLayer.makeSome[AppMongoClient, PostService](MongoPostDao.layer, PostService.layer)
