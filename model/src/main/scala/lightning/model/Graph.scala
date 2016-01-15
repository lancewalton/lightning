package lightning.model

import argonaut.Argonaut.casecodec2
import scalaz.Equal
import scalaz.syntax.equal.ToEqualOps
import scalaz.Monoid

case class Graph(nodes: Set[VisibilityNode] = Set.empty, dependencies: Set[VisibilityDependency] = Set.empty) {
  def +(that: Graph) = this ⊕⊕ that.nodes ⥅⥅ that.dependencies

  def ⊕⊕(nodes: Set[VisibilityNode]) = addNodes(nodes)
  def addNodes(nodes: Set[VisibilityNode]) = nodes.toList.foldLeft(this) { _ ⊕ _ }

  def ⊕(node: VisibilityNode) = addNode(node)
  def addNode(node: VisibilityNode) =
    replaceWithGreatestVisibility[Node](nodes, node, newNodes ⇒ copy(nodes = newNodes))

  def ⥅⥅(dependencies: Set[VisibilityDependency]) = addDependencies(dependencies)
  def addDependencies(dependencies: Set[VisibilityDependency]) = dependencies.toList.foldLeft(this) { _ ⥅ _ }

  def ⥅(dependency: VisibilityDependency) = addDependency(dependency)
  def addDependency(dependency: VisibilityDependency) =
    replaceWithGreatestVisibility[Dependency](dependencies, dependency, newDependencies ⇒ copy(dependencies = newDependencies))
      .addNode(Visibility(dependency.item.from, dependency.visible))
      .addNode(Visibility(dependency.item.to, dependency.visible))

  private def replaceWithGreatestVisibility[T: Equal](ts: Set[Visibility[T]], possibleUpdate: Visibility[T], update: Set[Visibility[T]] ⇒ Graph): Graph = {
    implicit val te = Visibility.equal[T]
    ts
      .find(t ⇒ t.item === possibleUpdate.item)
      .fold(update(ts + possibleUpdate)) { existing ⇒
        if (existing.visible) this
        else update(ts.filterNot(_.item === possibleUpdate.item) + possibleUpdate)
    }
  }
}

object Graph {
  val empty = Graph()

  implicit val timestampedNodeCode = Visibility.codec[Node]
  implicit val timestampedDependencyCode = Visibility.codec[Dependency]
  
  implicit val codec = casecodec2(Graph.apply, Graph.unapply)("nodes", "dependencies")
  
  implicit val monoid = new Monoid[Graph] {
    def zero = empty
    def append(g1: Graph, g2: => Graph) = g1 + g2
  }
}