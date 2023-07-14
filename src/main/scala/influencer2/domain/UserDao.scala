package influencer2.domain

import zio.IO

trait UserDao:
  def createUser(user: User): IO[UserAlreadyExists, Unit]
  def loadUser(username: String): IO[UserNotFound.type, User]
  def followUser(followeeUsername: String, followerId: UserId, followerUsername: String): IO[UserNotFound.type, Unit]
