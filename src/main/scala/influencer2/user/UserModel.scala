package influencer2.user

import zio.{UIO, ZIO}

import java.util.UUID

case class User(id: UserId, username: String, passwordHash: String)

opaque type UserId = UUID
object UserId:
  def apply(value: UUID): UserId                = value
  def random: UIO[UserId]                       = ZIO.random.flatMap(_.nextUUID)
  extension (id: UserId) inline def value: UUID = id

object InvalidCredentials
object UserNotFound
case object UserCreationConflict

private case class UserAlreadyExists(user: User)
