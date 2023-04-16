package influencer2.post

import influencer2.user.UserId
import zio.{UIO, ZIO}

import java.net.{URI, URL}
import java.time.Instant
import java.util.UUID
import scala.util.Try

case class Post(
    id: PostId,
    userId: UserId,
    username: String,
    createdAt: Instant,
    imageUrl: HttpsUrl,
    message: Option[String]
)

opaque type PostId = UUID
object PostId extends (UUID => PostId):
  def apply(value: UUID): PostId                   = value
  def random: UIO[PostId]                          = ZIO.random.flatMap(_.nextUUID)
  extension (value: PostId) inline def value: UUID = value

opaque type HttpsUrl = URL
object HttpsUrl:
  def create(value: String): Either[String, HttpsUrl] =
    Right(URL(value))
      .flatMap(url => Try(url.toURI).fold(e => Left(e.getMessage), _ => Right(url)))
      .flatMap { url =>
        if url.getProtocol.equalsIgnoreCase("https") then Right(url)
        else Left(s"protocol must be 'https' but was '${url.getProtocol}'")
      }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def createUnsafe(value: String): HttpsUrl =
    create(value).fold(e => throw RuntimeException(s"could not create HttpsUrl: $e"), identity)
  extension (value: HttpsUrl) inline def value: URL = value
