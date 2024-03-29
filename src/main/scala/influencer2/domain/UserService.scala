package influencer2.domain

import org.mindrot.jbcrypt.BCrypt
import zio.{Clock, IO, UIO, URLayer, ZIO, ZLayer}

class UserService(userDao: UserDao):
  def createUser(username: String, password: String): IO[UserCreationConflict.type, User] = for
    userId       <- UserId.random
    createdAt    <- Clock.instant
    passwordHash <- ZIO.succeedBlocking(BCrypt.hashpw(password, BCrypt.gensalt()))
    newUser = User(userId, createdAt, username, passwordHash, 0, 0, 0, Map.empty, Map.empty)
    user <- userDao.createUser(newUser).as(newUser).catchAll { case UserAlreadyExists(oldUser) =>
      ZIO.ifZIO(ZIO.succeedBlocking(BCrypt.checkpw(password, oldUser.passwordHash)))(
        ZIO.succeed(oldUser),
        ZIO.fail(UserCreationConflict)
      )
    }
  yield user

  def readUser(username: String): IO[UserNotFound.type, User] = userDao.loadUser(username)

  def followUser(followeeUsername: String, followerId: UserId, followerUsername: String): IO[UserNotFound.type, Unit] =
    userDao.followUser(followeeUsername, followerId, followerUsername)

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
