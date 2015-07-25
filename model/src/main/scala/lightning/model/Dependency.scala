package lightning.model

import argonaut.Argonaut.casecodec3
import scalaz.Equal.equalA

case class Dependency(from: Node, to: Node, label: DependencyLabel)

object Dependency {
  implicit val codec = casecodec3(Dependency.apply, Dependency.unapply)("from", "to", "label")

  implicit val equal = equalA[Dependency]
}