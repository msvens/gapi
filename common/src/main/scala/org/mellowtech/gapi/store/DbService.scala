package org.mellowtech.gapi.store

import org.mellowtech.gapi.config.GApiConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * @author msvens
  * @since 2017-05-14
  */
class DbService(implicit c: GApiConfig){


  //val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("",getC)
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("",c.slick.get)
  val profile = dbConfig.profile

  val db = dbConfig.db

  /*private def getC: Config = {
    val p = "profile = \""+c.dbProfile.get+"\"\n"
    val url = "url = \""+c.dbUrl.get+"\"\n"
    val user = "user = \""+c.dbUser.get+"\"\n"
    val pass = "password = \""+c.dbPassword+"\"\n"
    val confString = p+"db = {\n"+url+user+pass+"}"
    ConfigFactory.parseString(confString)
  }*/



}
