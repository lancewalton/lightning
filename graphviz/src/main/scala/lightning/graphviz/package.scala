package lightning

import java.util.Date

import lightning.model.{ Dependency, Graph, Node, TimestampedDependency, TimestampedNode }
import scalaz.Show
import scalaz.syntax.show._

package object graphviz {
  val successColor = "green"
  val failureColor = "red"

  def quote(s: String) = s""""$s""""
  
  def escape(s: String) = s

  def nodeId(node: Node) = quote(node.system + node.clusterNode.fold("") { "/" + _ })
  
  def subgraphName(node: Node) = quote(s"cluster_${node.system}")

  implicit object TimestampedNodeShow extends Show[TimestampedNode] {
    def label(node: Node) = Attribute("label", quote(node.clusterNode getOrElse node.system))
    def fillColor(timestamp: Option[Date]) = timestamp map { _ ⇒ Attribute("fillcolor", successColor) }
    def attributes(n: TimestampedNode) = (Attributes() + label(n.timestamped) + fillColor(n.timestamp))

    override def shows(n: TimestampedNode) = s"${nodeId(n.timestamped)} ${attributes(n)};"
  }

  implicit object TimestampedClusterNodesShow extends Show[Set[TimestampedNode]] {
    private def clusterNodes(ns: Set[TimestampedNode]) = ns.map { _.show }.mkString("\r\n|")
    
    override def shows(ns: Set[TimestampedNode]) =
      ns.headOption
        .fold("") { firstNode =>
          s"""|subgraph ${subgraphName(firstNode.timestamped)} {
              |label = "${firstNode.timestamped.system}";
              |${clusterNodes(ns)}
              |}"""
        }
  }
  
  implicit object TimestampedDependencyShow extends Show[TimestampedDependency] {
    def label(d: Dependency) = Attribute("label", d.label)
    def color(ts: Option[Date]) = ts map { _ => Attribute("color", successColor) }
    def attributes(d: TimestampedDependency) = (Attributes() + label(d.timestamped) + color(d.timestamp))
      
    override def shows(d: TimestampedDependency) = s"${nodeId(d.timestamped.from)} -> ${nodeId(d.timestamped.to)} ${attributes(d)};"
  }
  
  implicit object TimestampedDependencySetShow extends Show[Set[TimestampedDependency]] {
    override def shows(ds: Set[TimestampedDependency]) = ds.map { _.show }.mkString("\r\n|")
  }
  
  implicit object GraphShow extends Show[Graph] {
    private def showNodes(nodes: Set[TimestampedNode]) = nodes.groupBy { _.timestamped.system }.map(_._2.show).mkString("\r\n")
    
    override def shows(g: Graph) =
      s"""|digraph status {
          |node[shape=Mrecord, style=filled, fillcolor=$failureColor]
          |edge[color=$failureColor]
          |
          ${showNodes(g.nodes)}
          |
          |${g.dependencies.show}
          |
          |}""".stripMargin
  }
}