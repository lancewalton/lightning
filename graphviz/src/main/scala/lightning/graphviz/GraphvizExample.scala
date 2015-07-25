package lightning.renderer.graphviz

import java.util.Date

import lightning.model.{ Dependency, Graph, Node, SystemName, Timestamped, TimestampedDependency, TimestampedNode }
import scalaz.std.string._
import scalaz.syntax.std.option._

object GraphvizExample extends App {
  val n11 = Timestamped(Node("IB SS Intranet", Some("dev1")), Some(new Date))
  val n12 = Timestamped(Node("IB SS Intranet", Some("dev2")), None)
  val n2 = Timestamped(Node("Loganberry", None), Some(new Date))

  val g = Graph.empty ⊕
    n11 ⊕ n12 ⊕ n2 ⥅
    Timestamped(Dependency(n11.timestamped, n2.timestamped, "n11_n2"), None)

  import scalaz.syntax.show._
  println(g.show)
}