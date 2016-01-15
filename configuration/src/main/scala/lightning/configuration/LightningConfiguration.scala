package lightning.configuration

import lightning.evaluator.StatusEvaluatorSet

case class LightningConfiguration(httpPort: Int, services: Map[String, StatusEvaluatorSet])