package influencer2.user

import influencer2.user.UserDao.UserAlreadyExists
import zio.IO

trait UserDao:
  def createUser(user: User): IO[UserAlreadyExists, User]

object UserDao:
  case class UserAlreadyExists(user: User)
