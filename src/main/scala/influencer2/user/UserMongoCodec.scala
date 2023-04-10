package influencer2.user

import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.zio.json.deriveZioJsonCodecProvider
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

object UserMongoCodec:
  // custom MongoUser because there doesn't seem to be a way to translate the id field to _id w/ zio-json
  private case class MongoUser(_id: UUID, username: String, passwordHash: String):
    def toUser: User = User(UserId(_id), username, passwordHash)
  private object MongoUser:
    def fromUser(user: User): MongoUser = MongoUser(user.id.value, user.username, user.passwordHash)

  private val mongoUserCodec: JsonCodec[MongoUser] = DeriveJsonCodec.gen

  private given JsonCodec[User] =
    val encoder = mongoUserCodec.encoder.contramap[User](MongoUser.fromUser)
    val decoder = mongoUserCodec.decoder.map(_.toUser)
    JsonCodec(encoder, decoder)

  given MongoCodecProvider[User] = deriveZioJsonCodecProvider
