package lightning.evaluator.json

import lightning.model.{ DependencyLabel, SystemName }
import lightning.model.Node

sealed trait StatusResponse
case class SuccessfulStatusResponse(dependencyLabel: DependencyLabel, node: Node, graph: RootedGraphJson) extends StatusResponse
case class UnsuccessfulStatusResponse(dependencyLabel: DependencyLabel, node: Node) extends StatusResponse