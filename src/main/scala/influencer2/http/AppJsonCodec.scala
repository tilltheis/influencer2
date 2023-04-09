package influencer2.http

import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec, JsonDecoder, JsonEncoder}

object AppJsonCodec:
  given JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen

  given JsonDecoder[CreateUserRequest] = DeriveJsonDecoder.gen
  given JsonEncoder[UserResponse]        = DeriveJsonEncoder.gen

  given JsonDecoder[LoginRequest]  = DeriveJsonDecoder.gen
  given JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen

  given JsonCodec[SessionUser] = DeriveJsonCodec.gen
end AppJsonCodec
