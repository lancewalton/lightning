package lightning.configuration.loader

import scala.collection.JavaConverters.asScalaBufferConverter

import com.typesafe.config.{ Config, ConfigException }

import Read.StringRead
import scalaz.{ Failure, NonEmptyList, Success, Validation }
import scalaz.Validation.{ ValidationApplicative, ValidationFlatMapDeprecated }
import scalaz.std.list.listInstance
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.syntax.validation._
import shapeless.syntax.typeable.typeableOps

trait ConfigurationUtils {
  protected val config: Config
  
  import Read._
  
  protected def optional[A: Read](property: String, default: A): Validated[A] =
    optional[A](property).map(_.getOrElse(default))
  
  protected def optional[A: Read](property: String): Validated[Option[A]] = {
    val configValue: Validated[Option[String]] = untypedOptional(property)

    configValue.flatMap {
      case Some(v) => Read[A].read(v).map(Some(_))
      case None => Success(None)
    }
  }

  protected def untypedOptional(property: String): Validated[Option[String]] =
    Validation.fromTryCatchNonFatal(getString(property)) match {
      case Success(s) => Option(s).successNel[String]
      case Failure(f) if f.cast[ConfigException.Missing].isDefined ⇒ None.successNel[String]
      case Failure(f) => f.toString.failureNel[Option[String]]
    }

  protected def list[A: Read](property: String): Validated[List[A]] =
    toValidated[Validated[List[A]]](
      config.getStringList(property).asScala.toList.map(_.read[A]).sequence[Validated, A]
    ).flatMap(identity)


  protected def required[A: Read](property: String): Validated[A] =
    get(property).flatMap(v ⇒ if (v.isEmpty) s"Required property '$property' is empty".failure[A].toValidationNel else {
      v.read[A].leftMap(nel => nel append NonEmptyList(property))
    })

  protected def get(key: String): Validated[String] = toValidated(getString(key))

  protected def getString(key: String): String = {
    val result = config.getString(key)

    if (result.contains("$")) {
      sys.error("""Probable interpolation failure in '""" + key +
        s"""', use "blah"$$something"yada", not "blah$${something}yada" """)
    }

    result
  }
}