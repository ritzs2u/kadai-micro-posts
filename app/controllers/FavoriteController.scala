package controllers

import java.time.ZonedDateTime
import javax.inject._

import jp.t2v.lab.play2.auth.AuthenticationElement
import models.{Favorite, MicroPost, PagedItems}
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{FavoriteService, MicroPostService, UserService}
import skinny.Pagination

@Singleton
class FavoriteController @Inject()(val favoriteService: FavoriteService,
                                   val microPostService: MicroPostService,
                                   val userService: UserService,
                                   components: ControllerComponents)
    extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {

  def favorite(postId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    val now         = ZonedDateTime.now()
    val favorite  = Favorite(None, currentUser.id.get, postId, now, now)
    favoriteService
      .create(favorite)
      .map { _ =>
        Redirect(routes.HomeController.index())
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def unFavorite(postId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    favoriteService
      .deleteBy(currentUser.id.get, postId)
      .map { _ =>
        Redirect(routes.HomeController.index())
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

}
