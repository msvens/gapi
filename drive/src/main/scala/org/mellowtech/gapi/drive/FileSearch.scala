package org.mellowtech.gapi.drive

import java.time.LocalDateTime

/**
  * @author msvens
  * @since 2017-05-23
  */
trait Operation{
  def render: String
  def escape(input: String): String = {
    val s = new StringBuilder
    for(c <- input){
      c match {
        case '\\' => s.append("\\\\")
        case '\'' => s.append("\\\'")
        case c => s.append(c)
      }
    }
    s.toString
  }
}

trait NamedOperation extends Operation{
  def name: String
}

case class StringOp(val name: String, val op: String, val value: String) extends NamedOperation {
  val render: String = s"$name $op '${escape(value)}'"
}

case class InOp(val name: String, val value: String) extends NamedOperation {
  val render = s"'${escape(value)}' in $name"
}

case class BooleanOp(val name: String, val op: String, val value: Boolean) extends NamedOperation{
  val render = s"$name $op $value"
}

case class DateOp(val name: String, val op: String, val time: LocalDateTime) extends NamedOperation{
  val render = s"$name $op '${time.toString}'"
}

case class HasOp(val name: String, val key: String, val value: String) extends NamedOperation{
  val render = s"$name has {key='$key' and value='${escape(value)}'"
}

case class Clause(ops: Operation*) extends Operation{
  def render: String = {
    def r(oper: List[Operation]): String = oper match {
      case Nil => ""
      case o :: Nil => o match {
        case _ : Clause => "("+o.render+")"
        case _ => o.render
      }
      case o :: tail => o match {
        case _ : Clause => "("+o.render+") " +r(tail)
        case _ => o.render + " " + r(tail)
      }
    }
    r(ops.toList)
  }
}

//class

object Operators{

  class Name {
    private def op(op: String, v: String): Operation = StringOp("name", op, v)
    def contains(v: String): Operation = op("contains", v)
    def ==(v: String): Operation = op("=", v)
    def !=(v: String): Operation = op("!=", v)
  }

  class FullText {
    def contains(v: String): Operation = StringOp("fullText", "contains", v)
  }

  class MimeType {
    private def op(op: String, v: String): Operation = StringOp("mimeType", op, v)
    def contains(v: String): Operation = op("contains", v)
    def ==(v: String): Operation = op("=", v)
    def !=(v: String): Operation = op("!=", v)
  }

  class BooleanSome(val name: String) {
    def ==(v: Boolean) = BooleanOp(name, "=", v)
    def !=(v: Boolean) = BooleanOp(name, "!=", v)
  }

  class DateSome(val name: String) {
    def <=(v: LocalDateTime) = DateOp(name, "<=", v)
    def <(v: LocalDateTime) = DateOp(name, "<", v)
    def ==(v: LocalDateTime) = DateOp(name, "=", v)
    def !=(v: LocalDateTime) = DateOp(name, "!=", v)
    def >(v: LocalDateTime) = DateOp(name, ">", v)
    def >=(v: LocalDateTime) = DateOp(name, ">=", v)

  }

  class InSome(val name: String) {
    def in(v: String) = InOp(name, v)
  }

  class HasSome(val name: String) {
    def has(k: String, v: String) = HasOp(name, k, v)
  }

  case object and extends Operation{val render = "AND"}
  case object or extends Operation{val render = "OR"}
  case object not extends Operation{val render = "NOT"}

  val name = new Name
  val fullText = new Name
  val mimeType = new MimeType
  val modifiedTime = new DateSome("modifiedTime")
  val viewedByMeTime = new DateSome("viewedByMeTime")
  val trashed = new BooleanSome("thrashed")
  val starred = new BooleanSome("starred")
  val sharedWithMe = new BooleanSome("sharedWithMe")
  val parents = new InSome("parents")
  val owners = new InSome("owners")
  val writers = new InSome("writers")
  val readers = new InSome("readers")
  val properties = new HasSome("properties")
  val appProperties = new HasSome("appProperties")
}



object TestQuery extends App {
  import Operators._
  val c = Clause(name == "h\\ell\"o\'s")
  println(c.render)

}



