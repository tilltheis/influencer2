package influencer2.http

import influencer2.user.User

case class CreateUserRequest(password: String)

case class LoginRequest(username: String, password: String)
case class LoginResponse(token: String)

case class ErrorResponse(message: String)

case class UserResponse(username: String)
object UserResponse:
  def fromUser(user: User): UserResponse = UserResponse(user.username)

case class SessionUser(username: String)
object SessionUser:
  def fromUser(user: User): SessionUser = SessionUser(user.username)

val JwtSignatureCookieName: String = "jwt-signature"
