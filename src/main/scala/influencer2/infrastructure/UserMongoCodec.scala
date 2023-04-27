package influencer2.infrastructure

import influencer2.domain.{User, UserId}
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.zio.json.deriveZioJsonCodecProvider
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

object UserMongoCodec:
  // custom MongoUser because there doesn't seem to be a way to translate the id field to _id w/ zio-json
  private case class MongoUser(
      _id: UUID,
      username: String,
      passwordHash: String,
      postCount: Long,
      followerCount: Long,
      followeeCount: Long
  ):
    def toUser: User = User(UserId(_id), username, passwordHash, postCount, followerCount, followeeCount)
  private object MongoUser:
    def fromUser(user: User): MongoUser =
      MongoUser(user.id.value, user.username, user.passwordHash, user.postCount, user.followerCount, user.followeeCount)

  private val mongoUserCodec: JsonCodec[MongoUser] = DeriveJsonCodec.gen

  private given JsonCodec[User] =
    val encoder = mongoUserCodec.encoder.contramap[User](MongoUser.fromUser)
    val decoder = mongoUserCodec.decoder.map(_.toUser)
    JsonCodec(encoder, decoder)

  given MongoCodecProvider[User] = deriveZioJsonCodecProvider
