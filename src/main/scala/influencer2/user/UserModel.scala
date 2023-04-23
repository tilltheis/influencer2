package influencer2.user

import zio.{UIO, ZIO}

import java.util.UUID

case class User(
    id: UserId,
    username: String,
    passwordHash: String,
    postCount: Long,
    followerCount: Long,
    followeeCount: Long
)

opaque type UserId = UUID
object UserId extends (UUID => UserId):
  def apply(value: UUID): UserId                = value
  def random: UIO[UserId]                       = ZIO.random.flatMap(_.nextUUID)
  extension (id: UserId) inline def value: UUID = id

object InvalidCredentials
object UserNotFound {
  def toException: RuntimeException = new RuntimeException("User not found.")
}
case object UserCreationConflict

private case class UserAlreadyExists(user: User)
