package services

import javax.inject.Singleton

import models.{PagedItems, User, Favorite, MicroPost}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class FavoriteServiceImpl extends FavoriteService {

  override def create(favorite: Favorite)(implicit dbSession: DBSession): Try[Long] = Try {
    Favorite.create(favorite)
  }

  override def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[Favorite]] = Try {
    Favorite.where('userId -> userId).apply()
  }

  override def findByPostId(postId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[Favorite]] =
    Try {
      Favorite.where('postId -> postId).apply().headOption
    }
/*
  // userIdのユーザーをフォローするユーザーの集合を取得する
  override def findFollowersByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    countByFollowId(userId).map { size =>
      PagedItems(pagination, size,
        Favorite.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(Favorite.defaultAlias.followId, userId),
            pagination.limit,
            pagination.offset,
            Seq(Favorite.defaultAlias.id.desc)
          )
          .map(_.user.get)
      )
    }
  }
 */
  override def countByPostId(postId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.postId, postId))
  }

  // userIdのユーザーがお気に入りしているMicroPostの集合を取得する
  override def findFavoritesByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]] = {
    // 全体の母数を取得する
    countByUserId(userId).map { size =>
      PagedItems(pagination, size,
        Favorite.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(Favorite.defaultAlias.userId, userId),
            pagination.limit,
            pagination.offset,
            Seq(Favorite.defaultAlias.id.desc)
          )
          .map(_.favoritePost.get),
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    Favorite.allAssociations.countBy(sqls.eq(Favorite.defaultAlias.userId, userId))
  }

  override def deleteBy(userId: Long, postId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int] = Try {
    val c     = Favorite.column
    val count = Favorite.countBy(sqls.eq(c.userId, userId).and.eq(c.postId, postId))
    if (count == 1) {
      Favorite.deleteBy(
        sqls
          .eq(Favorite.column.userId, userId)
          .and(sqls.eq(Favorite.column.postId, postId))
      )
    } else 0
  }

}
