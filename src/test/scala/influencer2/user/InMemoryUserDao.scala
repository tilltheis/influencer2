package influencer2.user

import zio.{IO, Ref, ULayer, ZIO, ZLayer}

class InMemoryUserDao(state: Ref[Map[String, User]]) extends UserDao:
  override def createUser(user: User): IO[UserDao.UserAlreadyExists, User] =
    state
      .modify { users =>
        users.get(user.name) match
          case None    => (Right(user), users.updated(user.name, user))
          case Some(u) => (Left(UserDao.UserAlreadyExists(u)), users)
      }
      .flatMap(ZIO.from)

object InMemoryUserDao:
  def layer: ULayer[InMemoryUserDao] = ZLayer(Ref.make(Map.empty).map(InMemoryUserDao(_)))
