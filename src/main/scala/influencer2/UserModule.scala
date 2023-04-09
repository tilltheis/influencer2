package influencer2

import influencer2.user.{MongoUserDao, UserDao, UserService}
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, URLayer, ZEnvironment, ZIO, ZLayer}

object UserModule:
  val layer: RLayer[ZMongoDatabase, UserService & UserDao] =
    ZLayer.makeSome[ZMongoDatabase, UserService & UserDao](MongoUserDao.layer, UserService.layer)
