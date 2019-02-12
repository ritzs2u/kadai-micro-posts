package services

import models.{PagedItems, User, Favorite, MicroPost}
import scalikejdbc.{AutoSession, DBSession}
import skinny.Pagination

import scala.util.Try

trait FavoriteService {

  def create(favorite: Favorite)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[Favorite]]

  def findByPostId(postId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[Favorite]]
/*
  def findPostsByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]]
 */
  def findFavoritesByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def countByPostId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def deleteBy(userId: Long, postId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int]

}
