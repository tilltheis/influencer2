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
          .setOnInsert("passwordHash", user.passwordHash)
          .setOnInsert("postCount", 0)
          .setOnInsert("followerCount", 0)
          .setOnInsert("followeeCount", 0),
        FindOneAndUpdateOptions().upsert(true)
      )
      .orDie
      .flatMap {
        case Some(existingUser) => ZIO.fail(UserAlreadyExists(existingUser))
        case None               => ZIO.unit
      }

  override def loadUser(username: String): IO[UserNotFound.type, User] =
    collection.find(Filter.eq("username", username)).first.orDie.someOrFail(UserNotFound)

  override def incrementPostCount(userId: UserId): IO[UserNotFound.type, Unit] =
    collection
      .updateOne(Filter.eq("_id", userId.value.toString), Update.inc("postCount", 1))
      .orDie
      .filterOrFail(_.getModifiedCount == 1)(UserNotFound)
      .unit
end MongoUserDao

object MongoUserDao:
  val layer: RLayer[ZMongoDatabase, MongoUserDao] = ZLayer {
    for
      database   <- ZIO.service[ZMongoDatabase]
      collection <- database.getCollectionWithCodec[User]("users")
    yield MongoUserDao(collection)
  }
