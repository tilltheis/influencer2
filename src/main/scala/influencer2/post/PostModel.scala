package influencer2.post

import zio.{UIO, ZIO}

import java.util.UUID

case class Post(id: PostId)

opaque type PostId = UUID
object PostId extends (UUID => PostId):
  def apply(value: UUID): PostId                = value
  def random: UIO[PostId]                       = ZIO.random.flatMap(_.nextUUID)
  extension (id: PostId) inline def value: UUID = id
