package lightning.evaluator

import java.util.Date

import argonaut.Parse
import scalaz.std.iterable._
import scalaz.syntax.foldable._
import lightning.model.{ Dependency, Graph, Node, Timestamped }

case class StatusEvaluatorSet(node: Node, evaluators: Set[StatusEvaluator]) {
  def apply(): Graph = evaluators.toList.par.map { e => transformResponse(e()) }.seq.suml
 
  private def transformResponse(response: StatusResponse): Graph = {
    val timestamp = Some(new Date)
    val me = Timestamped(node, timestamp)
    response match {
    case ErrorStatusResponse(dl, nl, message) => Graph.empty ⊕ me ⥅ Timestamped(Dependency(node, nl, s"$dl ($message)"), None)
      case UnsuccessfulStatusResponse(dl, nl) ⇒ Graph.empty ⊕ me ⥅ Timestamped(Dependency(node, nl, dl), None)
      case SuccessfulStatusResponse(dl, g) => g.graph ⊕ me ⥅ Timestamped(Dependency(node, g.root, dl), timestamp)
    }
  }
}