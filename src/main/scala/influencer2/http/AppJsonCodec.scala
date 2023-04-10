package influencer2.http

import influencer2.post.PostId
import influencer2.user.UserId
import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec, JsonDecoder, JsonEncoder}

object AppJsonCodec:
  private given JsonEncoder[UserId] = JsonEncoder.uuid.contramap(_.value)
  private given JsonDecoder[UserId] = JsonDecoder.uuid.map(UserId)

  private given JsonEncoder[PostId] = JsonEncoder.uuid.contramap(_.value)
  private given JsonDecoder[PostId] = JsonDecoder.uuid.map(PostId)

  given JsonEncoder[MessageResponse] = DeriveJsonEncoder.gen

  given JsonDecoder[CreateUserRequest] = DeriveJsonDecoder.gen
  given JsonEncoder[UserResponse]      = DeriveJsonEncoder.gen

  given JsonDecoder[LoginRequest]  = DeriveJsonDecoder.gen
  given JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen

  given JsonDecoder[CreatePostRequest] = DeriveJsonDecoder.gen
  given JsonEncoder[PostResponse]      = DeriveJsonEncoder.gen

  given JsonCodec[SessionUser] = DeriveJsonCodec.gen
end AppJsonCodec
