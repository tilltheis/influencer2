package influencer2.domain

import zio.IO

trait UserDao:
  def createUser(user: User): IO[UserAlreadyExists, Unit]
  def loadUser(username: String): IO[UserNotFound.type, User]
