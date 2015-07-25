package lightning.model

import argonaut.Argonaut.casecodec2
import scalaz.Equal
import scalaz.syntax.equal.ToEqualOps
import scalaz.Monoid

case class Graph(nodes: Set[TimestampedNode] = Set.empty, dependencies: Set[TimestampedDependency] = Set.empty) {
  def +(that: Graph) = this ⊕⊕ that.nodes ⥅⥅ that.dependencies

  def ⊕⊕(nodes: Set[TimestampedNode]) = addNodes(nodes)
  def addNodes(nodes: Set[TimestampedNode]) = nodes.toList.foldLeft(this) { _ ⊕ _ }

  def ⊕(node: TimestampedNode) = addNode(node)
  def addNode(node: TimestampedNode) =
    replaceWithLatestTimestamp[Node](nodes, node, newNodes ⇒ copy(nodes = newNodes))

  def ⥅⥅(dependencies: Set[TimestampedDependency]) = addDependencies(dependencies)
  def addDependencies(dependencies: Set[TimestampedDependency]) = dependencies.toList.foldLeft(this) { _ ⥅ _ }

  def ⥅(dependency: TimestampedDependency) = addDependency(dependency)
  def addDependency(dependency: TimestampedDependency) =
    replaceWithLatestTimestamp[Dependency](dependencies, dependency, newDependencies ⇒ copy(dependencies = newDependencies))
      .addNode(Timestamped(dependency.timestamped.from, dependency.timestamp))
      .addNode(Timestamped(dependency.timestamped.to, dependency.timestamp))

  private def replaceWithLatestTimestamp[T: Equal](ts: Set[Timestamped[T]], possibleUpdate: Timestamped[T], update: Set[Timestamped[T]] ⇒ Graph): Graph = {
    implicit val te = Timestamped.equal[T]
    ts
      .find(t ⇒ t.timestamped === possibleUpdate.timestamped)
      .fold(update(ts + possibleUpdate)) { existing ⇒
        existing.timestamp.map((existing, _)).select(possibleUpdate.timestamp.map((possibleUpdate, _))) { (l, r) ⇒
          if (l._2 before r._2) l else r
        } map { case (replacement, _) ⇒ update(ts.filterNot(_ === existing) + replacement) } getOrElse this
      }
  }
}

object Graph {
  val empty = Graph()

  implicit val timestampedNodeCode = Timestamped.codec[Node]
  implicit val timestampedDependencyCode = Timestamped.codec[Dependency]
  
  implicit val codec = casecodec2(Graph.apply, Graph.unapply)("nodes", "dependencies")
  
  implicit val monoid = new Monoid[Graph] {
    def zero = empty
    def append(g1: Graph, g2: => Graph) = g1 + g2
  }
}