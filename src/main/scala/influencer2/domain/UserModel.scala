package influencer2.domain

import zio.{UIO, ZIO}

import java.time.Instant
import java.util.UUID

case class User(
    id: UserId,
    createdAt: Instant,
    username: String,
    passwordHash: String,
    postCount: Long,
    followerCount: Long,
    followeeCount: Long,
    followers: Map[UserId, String],
    followees: Map[UserId, String]
)

opaque type UserId = UUID
object UserId extends (UUID => UserId):
  def apply(value: UUID): UserId                = value
  def random: UIO[UserId]                       = ZIO.random.flatMap(_.nextUUID)
  extension (id: UserId) inline def value: UUID = id

object InvalidCredentials
object UserNotFound
case object UserCreationConflict

case class UserAlreadyExists(user: User)
