package lightning.evaluator

import lightning.model.{ DependencyLabel, SystemName }
import lightning.model.Node

sealed trait StatusResponse
case class SuccessfulStatusResponse(dependencyLabel: DependencyLabel, graph: RootedGraph) extends StatusResponse
case class UnsuccessfulStatusResponse(dependencyLabel: DependencyLabel, node: Node) extends StatusResponse
case class ErrorStatusResponse(dependencyLabel: DependencyLabel, node: Node, message: String) extends StatusResponse