package lightning.configuration

import java.net.{ URI, URL }

import scalaz.syntax.validation.ToValidationOps

trait Read[A] {
  def read(in: String): Validated[A]
}

object Read {
  def apply[A](implicit r: Read[A]): Read[A] = r

  implicit val stringReader = new Read[String] {
    override def read(in: String): Validated[String] = in.successNel[String]
  }

  implicit val boolReader = new Read[Boolean] {
    override def read(in: String): Validated[Boolean] = toValidated(in.toBoolean)
  }

  implicit val intReader = new Read[Int] {
    override def read(in: String): Validated[Int] = toValidated(in.toInt)
  }

  implicit val longReader = new Read[Long] {
    override def read(in: String): Validated[Long] = toValidated(in.toLong)
  }

  implicit val urlReader = new Read[URL] {
    override def read(in: String): Validated[URL] = toValidated(new URI(in).toURL)
  }

  implicit class StringRead(in: String) {
    def read[A: Read]: Validated[A] = implicitly[Read[A]].read(in)
  }
}