package org.mellowtech.gapi.store


import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.{ChronoUnit, TemporalUnit}

import org.mellowtech.gapi.model.TokenResponse

import scala.concurrent.{ExecutionContext, Future}

case class Token(id: String, access_token: String, token_type: String,
                 expires_in: LocalDateTime, refresh_token: Option[String])


trait TokenService {

  val defaultId = "c80624f5-3cd5-42a6-b2d9-7d76f47f17f1"

  /**
    * Puts a new or updates an existing token's access_token and expires_in. If the new token
    * also contains a refresh token that should be updated as well
    * @param t token to update
    * @return a future to the number of records that where updated (1)
    */
  def put(t: Token): Future[Int]


  def update(id: String, accessToken: String, expiresIn: LocalDateTime): Future[Int]

  /**
    * Deletes an existing token
    * @param id token to delete
    * @return a future to the numver of deleted redcords (0 or 1)
    */
  def delete(id: String): Future[Int]

  def get(id: String): Future[Option[Token]]

  def getDefault: Future[Option[Token]] = get(defaultId)

}

class MemTokenService extends TokenService {

  var m: Map[String,Token] = Map()

  /**
    * Puts a new or updates an existing token's access_token and expires_in. If the new token
    * also contains a refresh token that should be updated as well
    *
    * @param t token to update
    * @return a future to the number of records that where updated (1)
    */
  override def put(t: Token): Future[Int] = {
    val old: Token = m.getOrElse(t.id,t).copy(expires_in = t.expires_in, access_token = t.access_token)
    old.refresh_token match {
      case Some(_) => m += (old.id -> old)
      case None => m += (old.id -> old.copy(refresh_token = t.refresh_token))
    }
    Future.successful(1)
  }

  override def update(id: String, accessToken: String, expiresIn: LocalDateTime): Future[Int] = {
    val updated = m.get(id) match {
      case Some(n) => {
        m += (id -> n.copy(access_token = accessToken, expires_in = expiresIn))
        1
      }
      case None => 0
    }
    Future.successful(updated)
  }

  /**
    * Deletes an existing token
    *
    * @param id token to delete
    * @return a future to the numver of deleted redcords (0 or 1)
    */
  override def delete(id: String): Future[Int] = {
    val deleted = m.contains(id) match {
      case true => m -= id; 1
      case false => 0
    }
    Future.successful(deleted)
  }
  override def get(id: String): Future[Option[Token]] = Future.successful(m.get(id))
}


class TokenDAO(protected val dbService: DbService, implicit val ec: ExecutionContext) extends TokenService{

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


  //COMPILED QUERIES:
  def updateToken(id: Rep[String]) = for {
    t <- tokens if t.id === id
  } yield (t.access_token, t.expires_in)

  val updateTokenQ = Compiled(updateToken _)


  def tokenById(id: Rep[String]) = for {
    t <- tokens if t.id === id
  } yield t

  val tokenByIdQ = Compiled(tokenById _)


  /*private def update(id: String, accessToken: String, expiresIn: LocalDateTime): Future[Int] = {
    val q = for { t <- tokens if t.id === id } yield (t.access_token, t.expires_in)
    val updateAction = q.update((accessToken,expiresIn))
    dbService.db.run(updateAction)
  }*/

  override def update(id: String, accessToken: String, expriesIn: LocalDateTime): Future[Int] = {
    val action = updateTokenQ(id).update(accessToken, expriesIn)
    dbService.db.run(action)
  }

  override def put(t: Token): Future[Int] = for {
    ot <- get(t.id)
    i <- ot match {
      case None => dbService.db.run(tokens += t)
      case _ if t.refresh_token.isDefined => dbService.db.run(tokens.insertOrUpdate(t))
      case _ => update(t.id, t.access_token, t.expires_in)
    }
  } yield i

  /*
  override def put(t: Token): Future[Int] = t.refresh_token match {
    case None => {
      for {
        ret <- get(t.id)
        i <- ret match {
          case None => dbService.db.run(tokens += t)
          case Some(_) => update(t.id, t.access_token, t.expires_in)
        }
      } yield i
    }
    case Some(_) => dbService.db.run(tokens.insertOrUpdate(t))
  }
  */

  override def get(id: String): Future[Option[Token]] = dbService.db.run(tokenByIdQ(id).result.headOption)


  override def delete(id: String): Future[Int] = dbService.db.run(tokenByIdQ(id).delete)

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

  def apply(dbService: DbService)(implicit ec: ExecutionContext): TokenDAO = new TokenDAO(dbService, ec)

  def toToken(tr: TokenResponse): Token = toToken(defaultUUID, tr)

  def toToken(id: String, tr: TokenResponse): Token = Token(
      id = id,
      access_token = tr.access_token,
      token_type = tr.token_type,
      expires_in = expiresInTime(tr.expires_in),
      refresh_token = tr.refresh_token)


}
