package influencer2.application

import influencer2.application.AppJsonCodec.given
import influencer2.application.Controller.withJsonRequest
import influencer2.domain.{HttpsUrl, PostAlreadyLiked, PostId, PostNotFound, PostNotLiked, PostService}
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
  val handleReadAllPosts: UIO[Response] = postService.readAllPosts.map { posts =>
    Response.json(posts.map(PostResponse.fromPost).toJson)
  }

  // this should have limit and offset
  def handleReadUserPosts(username: String): UIO[Response] = postService.readUserPosts(username).map { posts =>
    Response.json(posts.map(PostResponse.fromPost).toJson)
  }

  def handleReadPost(postIdString: String): UIO[Response] =
    withPostId(postIdString) { postId =>
      postService
        .readPost(postId)
        .either
        .map {
          case Left(_)     => Response.json(MessageResponse("post not found").toJson).setStatus(Status.NotFound)
          case Right(post) => Response.json(PostResponse.fromPost(post).toJson)
        }
    }

  def handleLikePost(sessionUser: SessionUser, postIdString: String): UIO[Response] =
    withPostId(postIdString) { postId =>
      postService
        .likePost(sessionUser.userId, sessionUser.username, postId)
        .either
        .map {
          case Left(PostNotFound) =>
            Response.json(MessageResponse("post not found").toJson).setStatus(Status.NotFound)
          case Left(PostAlreadyLiked) | Right(()) =>
            Response.json(MessageResponse("post liked").toJson).setStatus(Status.Created)
        }
    }

  def handleUnlikePost(sessionUser: SessionUser, postIdString: String): UIO[Response] =
    withPostId(postIdString) { postId =>
      postService
        .unlikePost(sessionUser.userId, sessionUser.username, postId)
        .either
        .map {
          case Left(PostNotFound) =>
            Response.json(MessageResponse("post not found").toJson).setStatus(Status.NotFound)
          case Left(PostNotLiked) =>
            Response.json(MessageResponse("post not liked").toJson).setStatus(Status.NotFound)
          case Right(()) => Response.json(MessageResponse("post unliked").toJson).setStatus(Status.Ok)
        }
    }

  private def withPostId(postIdString: String)(f: PostId => UIO[Response]): UIO[Response] =
    ZIO
      .attempt(PostId(UUID.fromString(postIdString)))
      .orElseFail(Response.json(MessageResponse("post id must be UUID").toJson).setStatus(Status.BadRequest))
      .flatMap(f)
      .merge
end PostController

object PostController:
  val layer: URLayer[PostService, PostController] = ZLayer.fromFunction(PostController(_))
