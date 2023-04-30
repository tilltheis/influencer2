package influencer2.infrastructure

import influencer2.domain.{HttpsUrl, Post, PostId, UserId}
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.zio.json.deriveZioJsonCodecProvider
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.Instant
import java.util.UUID

object PostMongoCodec:
  // custom MongoPost because there doesn't seem to be a way to translate the id field to _id w/ zio-json
  private case class MongoPost(
      _id: UUID,
      userId: UUID,
      username: String,
      createdAt: Instant,
      imageUrl: String,
      message: Option[String],
      likes: Map[String, String]
  ):
    // this shouldn't throw but zio-json doesn't seem to allow propagating custom errors for map/contramap
    def toPost: Post = Post(
      PostId(_id),
      UserId(userId),
      username,
      createdAt,
      HttpsUrl.createUnsafe(imageUrl),
      message,
      likes.map { case (id, username) => (UserId(UUID.fromString(id)), username) }
    )
  private object MongoPost:
    def fromPost(post: Post): MongoPost =
      MongoPost(
        post.id.value,
        post.userId.value,
        post.username,
        post.createdAt,
        post.imageUrl.value.toExternalForm,
        post.message,
        post.likes.map { (id, username) => (id.value.toString, username) }
      )

  private val mongoPostCodec: JsonCodec[MongoPost] = DeriveJsonCodec.gen

  private given JsonCodec[Post] =
    val encoder = mongoPostCodec.encoder.contramap[Post](MongoPost.fromPost)
    val decoder = mongoPostCodec.decoder.map(_.toPost)
    JsonCodec(encoder, decoder)

  given MongoCodecProvider[Post] = deriveZioJsonCodecProvider
