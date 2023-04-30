package influencer2.application

import influencer2.application.AppJsonCodec.given
import influencer2.application.Controller.withJsonRequest
import influencer2.domain.{HttpsUrl, PostId, PostService}
import zio.http.model.Status
import zio.http.{Request, Response}
import zio.json.EncoderOps
import zio.{UIO, URLayer, ZIO, ZLayer}

import java.util.UUID

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

  def handleReadPost(postIdString: String): UIO[Response] =
    ZIO
      .attempt(PostId(UUID.fromString(postIdString)))
      .orElseFail(Response.json(MessageResponse("post id must be UUID").toJson).setStatus(Status.BadRequest))
      .flatMap { postId =>
        postService
          .readPost(postId)
          .mapBoth(
            _ => Response.json(MessageResponse("post not found").toJson).setStatus(Status.NotFound),
            post => Response.json(PostResponse.fromPost(post).toJson)
          )
      }
      .merge

  def handleLikePost(sessionUser: SessionUser, postIdString: String): UIO[Response] =
    ZIO
      .attempt(PostId(UUID.fromString(postIdString)))
      .orElseFail(Response.json(MessageResponse("post id must be UUID").toJson).setStatus(Status.BadRequest))
      .flatMap { postId =>
        postService
          .likePost(sessionUser.userId, sessionUser.username, postId)
          .mapBoth(
            _ => Response.json(MessageResponse("post not found").toJson).setStatus(Status.NotFound),
            _ => Response.json(MessageResponse("post liked").toJson).setStatus(Status.Created)
          )
      }
      .merge
end PostController

object PostController:
  val layer: URLayer[PostService, PostController] = ZLayer.fromFunction(PostController(_))
