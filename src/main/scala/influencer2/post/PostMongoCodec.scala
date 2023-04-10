package influencer2.post

import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.zio.json.deriveZioJsonCodecProvider
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

object PostMongoCodec:
  // custom MongoPost because there doesn't seem to be a way to translate the id field to _id w/ zio-json
  private case class MongoPost(_id: UUID):
    def toPost: Post = Post(PostId(_id))
  private object MongoPost:
    def fromPost(user: Post): MongoPost = MongoPost(user.id.value)

  private val mongoPostCodec: JsonCodec[MongoPost] = DeriveJsonCodec.gen

  private given JsonCodec[Post] =
    val encoder = mongoPostCodec.encoder.contramap[Post](MongoPost.fromPost)
    val decoder = mongoPostCodec.decoder.map(_.toPost)
    JsonCodec(encoder, decoder)

  given MongoCodecProvider[Post] = deriveZioJsonCodecProvider
