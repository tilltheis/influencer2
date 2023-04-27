package influencer2.domain

import org.mindrot.jbcrypt.BCrypt
import zio.{IO, UIO, URLayer, ZIO, ZLayer}

class UserService(userDao: UserDao):
  def createUser(username: String, password: String): IO[UserCreationConflict.type, User] = for
    userId       <- UserId.random
    passwordHash <- ZIO.succeedBlocking(BCrypt.hashpw(password, BCrypt.gensalt()))
    newUser = User(userId, username, passwordHash, 0, 0, 0)
    user <- userDao.createUser(newUser).as(newUser).catchAll { case UserAlreadyExists(oldUser) =>
      ZIO.ifZIO(ZIO.succeedBlocking(BCrypt.checkpw(password, oldUser.passwordHash)))(
        ZIO.succeed(oldUser),
        ZIO.fail(UserCreationConflict)
      )
    }
  yield user

  def readUser(username: String): IO[UserNotFound.type, User] = userDao.loadUser(username)

  def verifyCredentials(username: String, password: String): IO[InvalidCredentials.type, User] =
    userDao.loadUser(username).orElseFail(InvalidCredentials).flatMap { user =>
      ZIO.ifZIO(ZIO.succeedBlocking(BCrypt.checkpw(password, user.passwordHash)))(
        ZIO.succeed(user),
        ZIO.fail(InvalidCredentials)
      )
    }
end UserService

object UserService:
  val layer: URLayer[UserDao, UserService] = ZLayer.fromFunction(UserService(_))
