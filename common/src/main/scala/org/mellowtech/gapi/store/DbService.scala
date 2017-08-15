package org.mellowtech.gapi.store

import org.mellowtech.gapi.config.GApiConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * @author msvens
  * @since 2017-05-14
  */
class DbService(implicit c: GApiConfig){

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("",c.slick.get)
  val profile = dbConfig.profile

  val db = dbConfig.db



}
