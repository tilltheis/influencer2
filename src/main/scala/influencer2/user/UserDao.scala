package influencer2.user

import influencer2.user.UserDao.UserAlreadyExists
import zio.{IO, UIO}

trait UserDao:
  def createUser(user: User): IO[UserAlreadyExists, User]
  def loadUser(username: String): UIO[Option[User]]

object UserDao:
  case class UserAlreadyExists(user: User)
