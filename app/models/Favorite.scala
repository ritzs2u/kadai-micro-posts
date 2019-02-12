package models

import java.time.ZonedDateTime

import scalikejdbc._, jsr310._
import skinny.orm._
import skinny.orm.feature._

case class Favorite(id: Option[Long],
                      userId: Long,
                      postId: Long,
                      createAt: ZonedDateTime = ZonedDateTime.now(),
                      updateAt: ZonedDateTime = ZonedDateTime.now(),
                      user: Option[User] = None,
                      favoritePost: Option[MicroPost] = None)

object Favorite extends SkinnyCRUDMapper[Favorite] {

  lazy val u1 = User.createAlias("u1")

  lazy val userRef = belongsToWithAliasAndFkAndJoinCondition[User](
    right = User -> u1,
    fk = "userId",
    on = sqls.eq(defaultAlias.userId, u1.id),
    merge = (fp, m) => fp.copy(user = m)
  )

  lazy val m1 = MicroPost.createAlias("m1")

  lazy val postRef = belongsToWithAliasAndFkAndJoinCondition[MicroPost](
    right = MicroPost -> m1,
    fk = "postId",
    on = sqls.eq(defaultAlias.postId, m1.id),
    merge = (fp, m) => fp.copy(favoritePost = m)
  )

  lazy val allAssociations: CRUDFeatureWithId[Long, Favorite] = joins(userRef, postRef)

  override def tableName = "favorites"

  override def defaultAlias: Alias[Favorite] = createAlias("uf")

  override def extract(rs: WrappedResultSet, n: ResultName[Favorite]): Favorite =
    autoConstruct(rs, n, "user", "favoritePost")

  def create(favorite: Favorite)(implicit session: DBSession): Long =
    createWithAttributes(toNamedValues(favorite): _*)

  private def toNamedValues(record: Favorite): Seq[(Symbol, Any)] = Seq(
    'userId   -> record.userId,
    'postId -> record.postId,
    'createAt -> record.createAt,
    'updateAt -> record.updateAt
  )

  def update(favorite: Favorite)(implicit session: DBSession): Int =
    updateById(favorite.id.get).withAttributes(toNamedValues(favorite): _*)

}
