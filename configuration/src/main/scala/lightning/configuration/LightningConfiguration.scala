package lightning.configuration

import scala.collection.JavaConverters.asScalaBufferConverter
import com.typesafe.config.{ Config, ConfigException }
import Read.StringRead
import scalaz.{ Failure, NonEmptyList, Success, Validation }
import scalaz.Validation.{ ValidationApplicative, ValidationFlatMapDeprecated }
import scalaz.std.list.listInstance
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.syntax.validation.ToValidationOps
import shapeless.syntax.typeable.typeableOps
import lightning.evaluator.StatusEvaluator
import lightning.evaluator.json.HttpGraphStatusEvaluator
import lightning.model.Node
import lightning.evaluator.StatusEvaluatorSet
import lightning.evaluator.HttpOkStatusEvaluator

case class LightningConfiguration(config: Config, keyPrefix: String) {
  import Read._
  
  private def optional[A: Read](property: String, default: A): Validated[A] =
    optional[A](property).map(_.getOrElse(default))
  
  private def optional[A: Read](property: String): Validated[Option[A]] = {
    val configValue: Validated[Option[String]] = untypedOptional(property)

    configValue.flatMap {
      case Some(v) => Read[A].read(v).map(Some(_))
      case None => Success(None)
    }
  }

  private def untypedOptional(property: String): Validated[Option[String]] =
    Validation.fromTryCatchNonFatal(getString(property)) match {
      case Success(s) => Option(s).successNel[String]
      case Failure(f) if f.cast[ConfigException.Missing].isDefined ⇒ None.successNel[String]
      case Failure(f) => f.toString.failureNel[Option[String]]
    }

  private def list[A: Read](property: String): Validated[List[A]] =
    toValidated[Validated[List[A]]](
      config.getStringList(property).asScala.toList.map(_.read[A]).sequence[Validated, A]
    ).flatMap(identity)


  private def required[A: Read](property: String): Validated[A] =
    get(property).flatMap(v ⇒ if (v.isEmpty) s"Required property '$property' is empty".failure[A].toValidationNel else {
      v.read[A].leftMap(nel => nel append NonEmptyList(property))
    })

  private def get(key: String): Validated[String] = toValidated(getString(key))

  private def getString(key: String): String = {
    val result = config.getString(key)

    if (result.contains("$")) {
      sys.error("""Probable interpolation failure in '""" + key +
        s"""', use "blah"$$something"yada", not "blah$${something}yada" """)
    }

    result
  }
  
  private def readUrlEvaluator(environment: String, service: String)(f: (String, Node) => StatusEvaluator): Validated[StatusEvaluator] =
    for {
      url <- required[String](s"$keyPrefix.$environment.$service.url")
      targetNode <- optional[String](s"$keyPrefix.$environment.$service.node")
    } yield f(url, Node(service, targetNode))

  private def readEnabledService(environment: String, service: String): Validated[StatusEvaluator] =
    required[String](s"$keyPrefix.$environment.$service.type")
      .flatMap {
        case "HttpGraph" => readUrlEvaluator(environment, service) { HttpGraphStatusEvaluator(_, _).asStatusEvaluator }
        case "HttpPing" => readUrlEvaluator(environment, service) { HttpOkStatusEvaluator(_, _) }
        case unknown => Validation.failure(NonEmptyList(s"""$keyPrefix.$environment.$service.type = "$unknown" : unknown type"""))
      }
    
  private def readService(environment: String, service: String): Validated[Option[StatusEvaluator]] =
    required[Boolean](s"$keyPrefix.$environment.$service.enabled")
      .flatMap { if (_) readEnabledService(environment, service).map(Option(_)) else Validation.success(None) }
  
  private def readServices(environments: List[String], services: List[String]): Validated[Map[String, StatusEvaluatorSet]] = {
    val foo = for {
      env <- environments
      serv <- services
    } yield readService(env, serv).map(_.map(Tuple3(env, serv, _)))
    foo.sequenceU.map {
      _.flatten
       .groupBy(_._1)
       .map { case (env, evals) => (env, evals.map(_._3).toSet) }
       .map { case (env, evals) => (env, StatusEvaluatorSet(Node("Lightning", None), evals)) }
       .toMap
    }
  }
  
  def apply() = {
    for {
      environmentNames <- list[String](s"${keyPrefix}.environments")
      serviceNames <- list[String](s"${keyPrefix}.services")
      services <- readServices(environmentNames, serviceNames)
    } yield services
  }
}