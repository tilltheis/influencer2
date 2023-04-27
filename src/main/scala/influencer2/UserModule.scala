package influencer2

import influencer2.domain.UserService
import influencer2.infrastructure.MongoUserDao
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, ZLayer}

object UserModule:
  val layer: RLayer[ZMongoDatabase, UserService] =
    ZLayer.makeSome[ZMongoDatabase, UserService](MongoUserDao.layer, UserService.layer)
