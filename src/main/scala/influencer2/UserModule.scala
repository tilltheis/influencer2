package influencer2

import influencer2.user.{MongoUserDao, UserService}
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, ZEnvironment, ZIO, ZLayer}

case class UserModule(userService: UserService)

object UserModule:
  val layer: RLayer[DatabaseModule, UserModule] =
    val databaseLayer = ZLayer.service[DatabaseModule].map(x => ZEnvironment(x.get.database))
    databaseLayer >>> MongoUserDao.layer >>> UserService.layer >>> ZLayer.fromFunction(UserModule(_))
