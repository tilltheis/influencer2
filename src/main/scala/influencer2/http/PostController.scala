package influencer2.http

import influencer2.http.AppJsonCodec.given
import influencer2.http.Controller.withJsonRequest
import influencer2.post.{HttpsUrl, PostService}
import zio.http.model.Status
import zio.http.{Request, Response}
import zio.json.EncoderOps
import zio.{UIO, URLayer, ZIO, ZLayer}

class PostController(postService: PostService):
  def handleCreatePost(sessionUser: SessionUser, request: Request): UIO[Response] =
    withJsonRequest[CreatePostRequest](request) { createPostRequest =>
      HttpsUrl.create(createPostRequest.imageUrl) match
        case Left(_) =>
          ZIO.succeed(
            Response.json(MessageResponse("image url must be https").toJson).setStatus(Status.UnprocessableEntity)
          )

        case Right(imageUrl) =>
          val message = createPostRequest.message.map(_.trim).filter(_.nonEmpty)
          for post <- postService.createPost(sessionUser.userId, sessionUser.username, imageUrl, message)
          yield Response.json(PostResponse.fromPost(post).toJson).setStatus(Status.Created)
    }

  // this should have limit and offset
  val handleReadPosts: UIO[Response] = postService.readPosts.map { posts =>
    Response.json(posts.map(PostResponse.fromPost).toJson)
  }
end PostController

object PostController:
  val layer: URLayer[PostService, PostController] = ZLayer.fromFunction(PostController(_))
