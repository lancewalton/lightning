package lightning

import java.util.Date

import lightning.model.{ Dependency, Graph, Node, VisibilityDependency, VisibilityNode }
import scalaz.Show
import scalaz.syntax.show._
import scalaz.syntax.std.boolean._

package object graphviz {
  val successColor = "green"
  val failureColor = "red"

  def quote(s: String) = s""""$s""""
  
  def escape(s: String) = s

  def nodeId(node: Node) = quote(node.system + node.clusterNode.fold("") { "/" + _ })
  
  def subgraphName(node: Node) = quote(s"cluster_${node.system}")

  implicit object VisibilityNodeShow extends Show[VisibilityNode] {
    def label(node: Node) = Attribute("label", quote(node.clusterNode getOrElse node.system))
    def fillColor(visibility: Boolean) = visibility option Attribute("fillcolor", successColor)
    def attributes(n: VisibilityNode) = (Attributes() + label(n.item) + fillColor(n.visible))

    override def shows(n: VisibilityNode) = s"${nodeId(n.item)} ${attributes(n)};"
  }

  implicit object VisibilityClusterNodesShow extends Show[Set[VisibilityNode]] {
    private def clusterNodes(ns: Set[VisibilityNode]) = ns.map { _.show }.mkString("\r\n|")
    
    override def shows(ns: Set[VisibilityNode]) =
      ns.headOption
        .fold("") { firstNode =>
          s"""|subgraph ${subgraphName(firstNode.item)} {
              |label = "${firstNode.item.system}";
              |${clusterNodes(ns)}
              |}"""
        }
  }
  
  implicit object VisibilityDependencyShow extends Show[VisibilityDependency] {
    def label(d: Dependency) = Attribute("label", d.label)
    def color(visibility: Boolean) = visibility option Attribute("color", successColor)
    def attributes(d: VisibilityDependency) = (Attributes() + label(d.item) + color(d.visible))
      
    override def shows(d: VisibilityDependency) = s"${nodeId(d.item.from)} -> ${nodeId(d.item.to)} ${attributes(d)};"
  }
  
  implicit object VisibilityDependencySetShow extends Show[Set[VisibilityDependency]] {
    override def shows(ds: Set[VisibilityDependency]) = ds.map { _.show }.mkString("\r\n|")
  }
  
  implicit object GraphShow extends Show[Graph] {
    private def showNodes(nodes: Set[VisibilityNode]) = nodes.groupBy { _.item.system }.map(_._2.show).mkString("\r\n")
    
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