package influencer2.application

import influencer2.domain.{Post, User, UserId}

import java.time.Instant
import java.util.UUID

case class CreateUserRequest(password: String)

case class LoginRequest(username: String, password: String)
case class LoginResponse(token: String)

case class CreatePostRequest(imageUrl: String, message: Option[String])
case class PostResponse(
    id: UUID,
    userId: UUID,
    username: String,
    createdAt: Instant,
    imageUrl: String,
    message: Option[String],
    likes: Map[String, String]
)
object PostResponse:
  def fromPost(post: Post): PostResponse =
    PostResponse(
      post.id.value,
      post.userId.value,
      post.username,
      post.createdAt,
      post.imageUrl.value.toExternalForm,
      post.message,
      post.likes.map { case (id, username) => (id.value.toString, username) }
    )

case class MessageResponse(message: String)

case class UserResponse(id: UUID, username: String, postCount: Long, followerCount: Long, followeeCount: Long)
object UserResponse:
  def fromUser(user: User): UserResponse =
    UserResponse(user.id.value, user.username, user.postCount, user.followerCount, user.followeeCount)

case class SessionUser(id: UUID, username: String):
  def userId: UserId = UserId(id)
object SessionUser:
  def fromUser(user: User): SessionUser = SessionUser(user.id.value, user.username)

val JwtSignatureCookieName: String = "jwt-signature"
