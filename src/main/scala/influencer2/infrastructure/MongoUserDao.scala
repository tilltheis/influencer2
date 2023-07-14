package influencer2.infrastructure

import com.mongodb.client.model.FindOneAndUpdateOptions
import influencer2.domain.{User, UserAlreadyExists, UserDao, UserId, UserNotFound}
import influencer2.infrastructure.UserMongoCodec.given_MongoCodecProvider_User
import mongo4cats.bson.{BsonValue, Document}
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
              // this should be a mongo date type but that would make mongo codec derivation from json codec much harder...
              .setOnInsert("createdAt", user.createdAt.toString)
              .setOnInsert("username", user.username)
              .setOnInsert("passwordHash", user.passwordHash)
              .setOnInsert("postCount", 0)
              .setOnInsert("followers", Document.empty)
              .setOnInsert("followerCount", 0)
              .setOnInsert("followeeCount", 0)
              .setOnInsert("followees", Document.empty),
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

  def followUser(followeeUsername: String, followerId: UserId, followerUsername: String): IO[UserNotFound.type, Unit] =
    client
      .transactedWith { session =>
        import mongo4cats.bson.syntax.*
        for
          followeeOption <- client.userCollection
            .findOneAndUpdate(
              session,
              Filter.eq("username", followeeUsername),
              Update.set(s"followers.$followerId", followerUsername).inc("followerCount", 1)
            )
          result <- ZIO
            .from(followeeOption)
            .foldZIO(
              _ => ZIO.left(UserNotFound),
              followee =>
                client.userCollection
                  .updateOne(
                    session,
                    Filter.eq("username", followerUsername),
                    Update.set(s"followees.${followee.id.value.toString}", followeeUsername).inc("followeeCount", 1)
                  ) *> ZIO.right(())
            )
        yield result
      }
      .orDie
      .absolve
end MongoUserDao

object MongoUserDao:
  val layer: RLayer[AppMongoClient, MongoUserDao] = ZLayer.fromFunction(MongoUserDao(_))
