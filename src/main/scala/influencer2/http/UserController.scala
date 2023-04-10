package influencer2.http

import influencer2.http.Controller.withJsonRequest
import influencer2.user.UserService
import zio.http.model.Status
import zio.http.{Request, Response}
import zio.json.EncoderOps
import zio.{UIO, URLayer, ZLayer}
import AppJsonCodec.given

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
end UserController

object UserController:
  val layer: URLayer[UserService, UserController] = ZLayer.fromFunction(UserController(_))
