package lightning.model

import argonaut.Argonaut.casecodec2
import scalaz.Equal.equalA

case class Node(system: SystemName, clusterNode: Option[ClusterNodeName])

object Node {
  implicit val codec = casecodec2(Node.apply, Node.unapply)("system", "clusterNode")

  implicit val equal = equalA[Node]
}