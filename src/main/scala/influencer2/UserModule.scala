package influencer2

import influencer2.user.{MongoUserDao, UserService}
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, URLayer, ZEnvironment, ZIO, ZLayer}

case class UserModule(userService: UserService)

object UserModule:
  val layer: RLayer[DatabaseModule, UserModule] =
    DatabaseModule.databaseLayer >>> MongoUserDao.layer >>> UserService.layer >>> ZLayer.fromFunction(UserModule(_))

  val userServiceLayer: URLayer[UserModule, UserService] = ZLayer.fromFunction((_: UserModule).userService)
