package influencer2

import influencer2.user.{MongoUserDao, UserService}
import zio.{RLayer, ZIO, ZLayer}

case class UserModule(userService: UserService)

object UserModule:
  val layer: RLayer[DatabaseModule, UserModule] =
    ZLayer {
      for
        databaseModule <- ZIO.service[DatabaseModule]
        collection     <- databaseModule.database.getCollection("users")
      yield UserModule(UserService(MongoUserDao(collection)))
    }
