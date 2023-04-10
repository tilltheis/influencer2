package influencer2.user

import com.mongodb.client.model.FindOneAndUpdateOptions
import influencer2.user.UserMongoCodec.given_MongoCodecProvider_User
import mongo4cats.operations.{Filter, Update}
import mongo4cats.zio.{ZMongoCollection, ZMongoDatabase}
import zio.{IO, RLayer, ZIO, ZLayer}

import java.util.UUID

class MongoUserDao(collection: ZMongoCollection[User]) extends UserDao:
  override def createUser(user: User): IO[UserAlreadyExists, Unit] =
    collection
      .findOneAndUpdate(
        Filter.eq("username", user.username),
        Update
          .setOnInsert("_id", user.id.value.toString)
          .setOnInsert("username", user.username)
          .setOnInsert("passwordHash", user.passwordHash),
        FindOneAndUpdateOptions().upsert(true)
      )
      .orDie
      .flatMap {
        case Some(existingUser) => ZIO.fail(UserAlreadyExists(existingUser))
        case None               => ZIO.unit
      }

  override def loadUser(username: String): IO[UserNotFound.type, User] =
    collection.find(Filter.eq("username", username)).first.orDie.someOrFail(UserNotFound)
end MongoUserDao

object MongoUserDao:
  val layer: RLayer[ZMongoDatabase, MongoUserDao] = ZLayer {
    for
      database   <- ZIO.service[ZMongoDatabase]
      collection <- database.getCollectionWithCodec[User]("users")
    yield MongoUserDao(collection)
  }
