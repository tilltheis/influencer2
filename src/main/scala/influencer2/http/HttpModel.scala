package influencer2.http

import influencer2.post.Post
import influencer2.user.{User, UserId}

import java.time.Instant
import java.util.UUID

case class CreateUserRequest(password: String)

case class LoginRequest(username: String, password: String)
case class LoginResponse(token: String)

case class CreatePostRequest(imageUrl: String, message: Option[String])
case class PostResponse(id: UUID, author: UUID, createdAt: Instant, imageUrl: String, message: Option[String])
object PostResponse:
  def fromPost(post: Post): PostResponse =
    PostResponse(post.id.value, post.author.value, post.createdAt, post.imageUrl.value.toExternalForm, post.message)

case class MessageResponse(message: String)

case class UserResponse(id: UUID, username: String)
object UserResponse:
  def fromUser(user: User): UserResponse = UserResponse(user.id.value, user.username)

case class SessionUser(id: UUID, username: String):
  def userId: UserId = UserId(id)
object SessionUser:
  def fromUser(user: User): SessionUser = SessionUser(user.id.value, user.username)

val JwtSignatureCookieName: String = "jwt-signature"
