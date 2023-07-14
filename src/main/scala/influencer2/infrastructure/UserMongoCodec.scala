package influencer2.infrastructure

import influencer2.domain.{User, UserId}
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.zio.json.deriveZioJsonCodecProvider
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.Instant
import java.util.UUID

object UserMongoCodec:
  // custom MongoUser because there doesn't seem to be a way to translate the id field to _id w/ zio-json
  private case class MongoUser(
      _id: UUID,
      createdAt: Instant,
      username: String,
      passwordHash: String,
      postCount: Long,
      followerCount: Long,
      followeeCount: Long,
      followers: Map[String, String],
      followees: Map[String, String]
  ):
    def toUser: User =
      User(
        UserId(_id),
        createdAt,
        username,
        passwordHash,
        postCount,
        followerCount,
        followeeCount,
        followers.map { case (id, username) => (UserId(UUID.fromString(id)), username) },
        followees.map { case (id, username) => (UserId(UUID.fromString(id)), username) }
      )
  private object MongoUser:
    def fromUser(user: User): MongoUser =
      MongoUser(
        user.id.value,
        user.createdAt,
        user.username,
        user.passwordHash,
        user.postCount,
        user.followerCount,
        user.followeeCount,
        user.followers.map { (id, username) => (id.value.toString, username) },
        user.followees.map { (id, username) => (id.value.toString, username) }
      )

  private val mongoUserCodec: JsonCodec[MongoUser] = DeriveJsonCodec.gen

  private given JsonCodec[User] =
    val encoder = mongoUserCodec.encoder.contramap[User](MongoUser.fromUser)
    val decoder = mongoUserCodec.decoder.map(_.toUser)
    JsonCodec(encoder, decoder)

  given MongoCodecProvider[User] = deriveZioJsonCodecProvider
