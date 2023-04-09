package influencer2.user

import zio.{IO, Ref, UIO, ULayer, ZIO, ZLayer}

class InMemoryUserDao(state: Ref[Map[String, User]]) extends UserDao:
  override def createUser(user: User): IO[UserDao.UserAlreadyExists, User] =
    state
      .modify { users =>
        users.get(user.username) match
          case None    => (Right(user), users.updated(user.username, user))
          case Some(u) => (Left(UserDao.UserAlreadyExists(u)), users)
      }
      .flatMap(ZIO.from)

  def loadUser(username: String): UIO[Option[User]] = state.get.map(_.get(username))

object InMemoryUserDao:
  val layer: ULayer[InMemoryUserDao] = ZLayer(Ref.make(Map.empty).map(InMemoryUserDao(_)))
