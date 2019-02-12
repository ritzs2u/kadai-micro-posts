package controllers

import javax.inject._

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{MicroPost, PagedItems}
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{MicroPostService, UserService, FavoriteService}
import skinny.Pagination

@Singleton
class HomeController @Inject()(val userService: UserService, val microPostService: MicroPostService, val favoriteService: FavoriteService, components: ControllerComponents)
    extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with OptionalAuthElement {

  private val postForm = Form {
    "content" -> nonEmptyText
  }

  def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val userOpt = loggedIn
    userOpt
      .map { user =>
        favoriteService.findFavoritesByUserId(Pagination(10, page), user.id.get)
          .map { favorites =>
            microPostService
              .findAllByWithLimitOffset(Pagination(10, page), user.id.get)
              .map { pagedItems =>
                Ok(views.html.index(userOpt, postForm, pagedItems, favorites))
              }
              .recover {
                case e: Exception =>
                  Logger.error(s"occurred error", e)
                  Redirect(routes.HomeController.index(page))
                    .flashing("failure" -> Messages("InternalError"))
              }
              .getOrElse(InternalServerError(Messages("InternalError")))
          }
          .recover {
            case e: Exception =>
              Logger.error(s"occurred error", e)
              Redirect(routes.HomeController.index(page))
                .flashing("failure" -> Messages("InternalError"))
          }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }
      .getOrElse(
        Ok(views.html.index(userOpt, postForm, PagedItems(Pagination(10, page), 0, Seq.empty[MicroPost]), PagedItems(Pagination(10, page), 0, Seq.empty[MicroPost])))
      )
  }


  def favorite(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val userOpt = loggedIn
    userOpt
      .map { user =>
        favoriteService.findFavoritesByUserId(Pagination(10, page), user.id.get)
          .map { favorites =>
            microPostService
              .findAllByWithLimitOffset(Pagination(10, page), user.id.get)
              .map { pagedItems =>
                val favoritePosts = for(micropost <- pagedItems.items if favorites.items.exists(_.id == micropost.id)) yield micropost
                val favoritePagedItems = PagedItems(pagedItems.pagination, pagedItems.totalCount, favoritePosts)
                Ok(views.html.favorite.index(userOpt, postForm, favoritePagedItems, favorites))
              }
              .recover {
                case e: Exception =>
                  Logger.error(s"occurred error", e)
                  Redirect(routes.HomeController.index(page))
                    .flashing("failure" -> Messages("InternalError"))
              }
              .getOrElse(InternalServerError(Messages("InternalError")))
          }
          .recover {
            case e: Exception =>
              Logger.error(s"occurred error", e)
              Redirect(routes.HomeController.index(page))
                .flashing("failure" -> Messages("InternalError"))
          }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }
      .getOrElse(
        Ok(views.html.favorite.index(userOpt, postForm, PagedItems(Pagination(10, page), 0, Seq.empty[MicroPost]), PagedItems(Pagination(10, page), 0, Seq.empty[MicroPost])))
      )
  }
}
