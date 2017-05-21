package org.mellowtech.gapi.store


import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.{ChronoUnit, TemporalUnit}

import org.mellowtech.gapi.model.TokenResponse

import scala.concurrent.{ExecutionContext, Future}

case class Token(id: String, access_token: String, token_type: String,
                 expires_in: LocalDateTime, refresh_token: Option[String])


class TokenDAO(protected val dbService: DbService) {

  import dbService.profile.api._

  implicit val LocalDateTimeToTimestamp = MappedColumnType.base[LocalDateTime, Timestamp](
    l => Timestamp.valueOf(l),
    t => t.toLocalDateTime
  )

  class TokenTable(tag: Tag) extends Table[Token](tag, "token") {
    def id = column[String]("id", O.PrimaryKey) // This is the primary key column
    def access_token = column[String]("access_token")

    def token_type = column[String]("token_type")

    def expires_in = column[LocalDateTime]("expires_in")

    def refresh_token = column[Option[String]]("refresh_token")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, access_token, token_type, expires_in, refresh_token) <> (Token.tupled, Token.unapply _)

  }

  private val tokens = TableQuery[TokenTable]


  def update(id: String, accessToken: String, expiresIn: LocalDateTime): Future[Int] = {
    val q = for { t <- tokens if t.id === id } yield (t.access_token, t.expires_in)
    val updateAction = q.update((accessToken,expiresIn))
    dbService.db.run(updateAction)
  }

  def put(t: Token)(implicit ec: ExecutionContext): Future[Int] = t.refresh_token match {
    case None => {
      for {
        ret <- get(t.id)
        i <- ret match {
          case None => forcePut(t)
          case Some(tt) => update(t.id, t.access_token, t.expires_in)
        }
      } yield i
    }
    case Some(_) => forcePut(t)
  }
  /**
    * Inserts a new token or updates an existing token
    */
  def forcePut(t: Token): Future[Int] = {
    dbService.db.run(tokens.insertOrUpdate(t))
  }

  def getDefault: Future[Option[Token]] = get(TokenDAO.defaultUUID)

  def get(id: String): Future[Option[Token]] = {
    val q = tokens.filter(_.id === id)
    dbService.db.run(q.result.headOption)
  }

}

object TokenDAO {


  def expiresInSecs(lt: LocalDateTime): Long = {
    LocalDateTime.now().until(lt, ChronoUnit.SECONDS) match {
      case x if x > 0 => x
      case _ => 0
    }
  }

  def expiresInTime(expires_in: Long): LocalDateTime = LocalDateTime.now().plusSeconds(expires_in)

  val defaultUUID = "c80624f5-3cd5-42a6-b2d9-7d76f47f17f1"

  def apply(dbService: DbService): TokenDAO = new TokenDAO(dbService)

  def toToken(tr: TokenResponse): Token = toToken(defaultUUID, tr)

  def toToken(id: String, tr: TokenResponse): Token = Token(
      id = id,
      access_token = tr.access_token,
      token_type = tr.token_type,
      expires_in = expiresInTime(tr.expires_in),
      refresh_token = tr.refresh_token)


}
