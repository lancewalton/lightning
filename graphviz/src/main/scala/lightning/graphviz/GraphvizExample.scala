package lightning.graphviz

import java.util.Date

import lightning.model.{ Dependency, Graph, Node, SystemName, Visibility, VisibilityDependency, VisibilityNode }
import scalaz.std.string._
import scalaz.syntax.std.option._

object GraphvizExample extends App {
  val n11 = Visibility(Node("IB SS Intranet", Some("dev1")), true)
  val n12 = Visibility(Node("IB SS Intranet", Some("dev2")), false)
  val n2 = Visibility(Node("Loganberry", None), true)

  val g = Graph.empty ⊕
    n11 ⊕ n12 ⊕ n2 ⥅
    Visibility(Dependency(n11.item, n2.item, "n11_n2"), false)

  import scalaz.syntax.show._
  println(g.show)
  
  println
  println(Graph.codec.encode(g).spaces2)
}