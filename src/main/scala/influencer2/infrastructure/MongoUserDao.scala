package influencer2.infrastructure

import com.mongodb.client.model.FindOneAndUpdateOptions
import influencer2.domain.{User, UserAlreadyExists, UserDao, UserNotFound}
import influencer2.infrastructure.UserMongoCodec.given_MongoCodecProvider_User
import mongo4cats.operations.{Filter, Update}
import mongo4cats.zio.{ZMongoCollection, ZMongoDatabase}
import zio.{IO, RLayer, ZIO, ZLayer}

class MongoUserDao(client: AppMongoClient) extends UserDao:
  override def createUser(user: User): IO[UserAlreadyExists, Unit] =
    client
      .sessionedWith { session =>
        client.userCollection
          .findOneAndUpdate(
            session,
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
      }
      .orDie
      .flatMap {
        case Some(existingUser) => ZIO.fail(UserAlreadyExists(existingUser))
        case None               => ZIO.unit
      }

  override def loadUser(username: String): IO[UserNotFound.type, User] =
    client
      .sessionedWith { session =>
        client.userCollection.find(session, Filter.eq("username", username)).first
      }
      .orDie
      .someOrFail(UserNotFound)
end MongoUserDao

object MongoUserDao:
  val layer: RLayer[AppMongoClient, MongoUserDao] = ZLayer.fromFunction(MongoUserDao(_))
