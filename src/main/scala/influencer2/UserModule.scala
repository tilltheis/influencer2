package influencer2

import influencer2.domain.UserService
import influencer2.infrastructure.{AppMongoClient, MongoUserDao}
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, ZLayer}

object UserModule:
  val layer: RLayer[AppMongoClient, UserService] =
    ZLayer.makeSome[AppMongoClient, UserService](MongoUserDao.layer, UserService.layer)
