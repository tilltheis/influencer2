package influencer2.user

import com.mongodb.client.model.FindOneAndUpdateOptions
import influencer2.user.UserDao.UserAlreadyExists
import mongo4cats.bson.Document
import mongo4cats.operations.{Filter, Update}
import mongo4cats.zio.ZMongoCollection
import zio.{IO, ZIO}

import java.util.UUID

class MongoUserDao(collection: ZMongoCollection[Document]) extends UserDao:
  override def createUser(user: User): IO[UserAlreadyExists, User] =
    collection
      .findOneAndUpdate(
        Filter.eq("name", user.name),
        Update
          .setOnInsert("_id", user.id.value.toString)
          .setOnInsert("name", user.name)
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
end MongoUserDao
