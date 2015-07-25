package lightning.evaluator

import argonaut.Argonaut.casecodec2
import lightning.model.{ Graph, Node }

case class RootedGraph(graph: Graph, root: Node)

object RootedGraph {
  implicit val codec = casecodec2(RootedGraph.apply, RootedGraph.unapply)("graph", "root")
}