package lightning.configuration

import com.typesafe.config.Config

import lightning.configuration.loader.{ ConfigurationUtils, Validated }
import lightning.evaluator.{ HttpOkStatusEvaluator, StatusEvaluator, StatusEvaluatorSet }
import lightning.evaluator.json.HttpGraphStatusEvaluator
import lightning.model.Node
import scalaz.{ NonEmptyList, Validation }
import scalaz.Validation._
import scalaz.std.list.listInstance
import scalaz.syntax.traverse.ToTraverseOps

case class EnvironmentConfigurationLoader(environment: String, config: Config) extends ConfigurationUtils {
  private def readUrlEvaluator(service: String)(f: (String, Node) ⇒ StatusEvaluator): Validated[StatusEvaluator] =
    for {
      url ← required[String](s"$service.url")
      targetNode ← optional[String](s"$service.node")
    } yield f(url, Node(service, targetNode))

  private def readEnabledService(service: String): Validated[StatusEvaluator] =
    required[String](s"$service.type")
      .flatMap {
        case "HttpGraph" ⇒ readUrlEvaluator(service) { HttpGraphStatusEvaluator(_, _).asStatusEvaluator }
        case "HttpPing"  ⇒ readUrlEvaluator(service) { HttpOkStatusEvaluator(_, _) }
        case unknown     ⇒ Validation.failure(NonEmptyList(s"""$service.type = "$unknown" : unknown service type in environment $environment"""))
      }

  private def readService(service: String): Validated[Option[StatusEvaluator]] =
    required[Boolean](s"$service.enabled")
      .flatMap { if (_) readEnabledService(service).map(Option(_)) else Validation.success(None) }

  def load(services: List[String]): Validated[StatusEvaluatorSet] =
    services
      .map { service ⇒ readService(service) }
      .sequenceU
      .map { evals ⇒ StatusEvaluatorSet(Node("Lightning", None), evals.flatten.toSet) }
}