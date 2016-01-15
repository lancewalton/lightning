package lightning.evaluator

import argonaut.Parse
import scalaz.std.iterable._
import scalaz.syntax.foldable._
import lightning.model.{ Dependency, Graph, Node, Visibility }
import lightning.model.Visibility

case class StatusEvaluatorSet(node: Node, evaluators: Set[StatusEvaluator]) {
  def apply(): Graph = evaluators.toList.par.map { e => transformResponse(e()) }.seq.suml
 
  private def transformResponse(response: StatusResponse): Graph = {
    val me = Visibility(node, true)
    response match {
    case ErrorStatusResponse(dl, nl, message) => Graph.empty ⊕ me ⥅ Visibility(Dependency(node, nl, s"$dl ($message)"), false)
      case UnsuccessfulStatusResponse(dl, nl) ⇒ Graph.empty ⊕ me ⥅ Visibility(Dependency(node, nl, dl), false)
      case SuccessfulStatusResponse(dl, g) => g.graph ⊕ me ⥅ Visibility(Dependency(node, g.root, dl), true)
    }
  }
}