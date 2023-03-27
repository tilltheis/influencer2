package influencer2.user

import influencer2.user.UserDao.UserAlreadyExists
import influencer2.user.UserService.UserCreationConflict
import org.mindrot.jbcrypt.BCrypt
import zio.{IO, URLayer, ZIO, ZLayer}

class UserService(userDao: UserDao):
  def createUser(createUser: CreateUser): IO[UserCreationConflict.type, User] = for
    userId       <- UserId.random
    passwordHash <- ZIO.succeedBlocking(BCrypt.hashpw(createUser.password, BCrypt.gensalt()))
    newUser = User(userId, createUser.name, passwordHash)
    user <- userDao.createUser(newUser).catchAll { case UserAlreadyExists(oldUser) =>
      ZIO.ifZIO(ZIO.succeedBlocking(BCrypt.checkpw(createUser.password, oldUser.passwordHash)))(
        ZIO.succeed(oldUser),
        ZIO.fail(UserCreationConflict)
      )
    }
  yield user
end UserService

object UserService:
  case object UserCreationConflict

  val layer: URLayer[UserDao, UserService] = ZLayer.fromFunction(UserService(_))
