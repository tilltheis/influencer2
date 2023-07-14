package influencer2.application

import influencer2.application.Controller.withJsonRequest
import zio.http.model.Status
import zio.http.{Request, Response}
import zio.json.EncoderOps
import zio.{UIO, URLayer, ZLayer}
import AppJsonCodec.given
import influencer2.domain.UserService

class UserController(userService: UserService):
  def handleCreateUser(username: String, request: Request): UIO[Response] =
    withJsonRequest[CreateUserRequest](request) { createUser =>
      userService
        .createUser(username, createUser.password)
        .fold(
          _ => Response.json(MessageResponse("username already taken").toJson).setStatus(Status.Conflict),
          user => Response.json(UserResponse.fromUser(user).toJson).setStatus(Status.Created)
        )
    }

  def handleReadUser(username: String): UIO[Response] =
    userService
      .readUser(username)
      .fold(
        _ => Response.json(MessageResponse("user not found").toJson).setStatus(Status.NotFound),
        user => Response.json(UserResponse.fromUser(user).toJson)
      )

  def handleFollowUser(followeeUsername: String, follower: SessionUser, request: Request): UIO[Response] =
    userService
      .followUser(followeeUsername, follower.userId, follower.username)
      .fold(
        _ => Response.json(MessageResponse("user not found").toJson).setStatus(Status.NotFound),
        _ => Response.json(MessageResponse("user followed").toJson).setStatus(Status.Created)
      )
end UserController

object UserController:
  val layer: URLayer[UserService, UserController] = ZLayer.fromFunction(UserController(_))
