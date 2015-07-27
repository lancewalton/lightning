package lightning.configuration

import com.typesafe.config.Config

import lightning.configuration.loader.{ ConfigurationUtils, FallbackConfigurationLoader, Validated }
import lightning.evaluator.StatusEvaluatorSet
import scalaz.Validation._
import scalaz.std.list._
import scalaz.syntax.traverse.ToTraverseOps

case class LightningConfigurationLoader(config: Config) extends ConfigurationUtils {
  private def readEnvironment(environment: String, services: List[String]): Validated[StatusEvaluatorSet] =
    EnvironmentConfigurationLoader(environment, FallbackConfigurationLoader.loadOrDie(environment)).load(services)

  private def readStatusEvaluatorSetsByEnvironment(environments: List[String], services: List[String]): Validated[Map[String, StatusEvaluatorSet]] =
    environments
      .map { env ⇒ readEnvironment(env, services).map(ses ⇒ env -> ses) }
      .sequenceU
      .map { _.toMap }

  def apply() = {
    for {
      environmentNames ← list[String]("environments")
      serviceNames ← list[String]("services")
      services ← readStatusEvaluatorSetsByEnvironment(environmentNames, serviceNames)
    } yield services
  }
}