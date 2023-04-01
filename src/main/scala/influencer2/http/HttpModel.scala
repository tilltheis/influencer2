package influencer2.http

import influencer2.user.User

case class Login(username: String, password: String)

case class SessionUser(username: String)
object SessionUser:
  def fromUser(user: User): SessionUser = SessionUser(user.username)

val JwtSignatureCookieName: String     = "jwt-signature"
val JwtHeaderPayloadCookieName: String = "jwt-header.payload"
