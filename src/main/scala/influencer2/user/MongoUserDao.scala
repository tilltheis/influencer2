package influencer2.user

import com.mongodb.client.model.FindOneAndUpdateOptions
import mongo4cats.bson.Document
import mongo4cats.operations.{Filter, Update}
import mongo4cats.zio.{ZMongoCollection, ZMongoDatabase}
import zio.{IO, RLayer, UIO, ZIO, ZLayer}

import java.util.UUID

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
class MongoUserDao(collection: ZMongoCollection[Document]) extends UserDao:
  override def createUser(user: User): IO[UserAlreadyExists, User] =
    collection
      .findOneAndUpdate(
        Filter.eq("name", user.username),
        Update
          .setOnInsert("_id", user.id.value.toString)
          .setOnInsert("name", user.username)
          .setOnInsert("passwordHash", user.passwordHash),
        FindOneAndUpdateOptions().upsert(true)
      )
      .orDie
      .flatMap {
        case Some(document) =>
          ZIO.fail(
            UserAlreadyExists(
              User(
                UserId(UUID.fromString(document.get("_id").get.asString.get)),
                document.get("name").get.asString.get,
                document.get("passwordHash").get.asString.get
              )
            )
          )
        case None => ZIO.succeed(user)
      }

  override def loadUser(username: String): IO[UserNotFound.type, User] =
    collection.find(Filter.eq("name", username)).first.orDie.someOrFail(UserNotFound).map { document =>
      User(
        UserId(UUID.fromString(document.get("_id").get.asString.get)),
        document.get("name").get.asString.get,
        document.get("passwordHash").get.asString.get
      )
    }
end MongoUserDao

object MongoUserDao:
  val layer: RLayer[ZMongoDatabase, MongoUserDao] = ZLayer {
    for
      database   <- ZIO.service[ZMongoDatabase]
      collection <- database.getCollection("users")
    yield MongoUserDao(collection)
  }
