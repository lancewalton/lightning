package lightning

import java.text.SimpleDateFormat
import java.util.Date

import argonaut.{ CodecJson, DecodeResult }
import argonaut.Argonaut.{ StringDecodeJson, StringEncodeJson }
import scalaz.{ \/ ⇒ \/ }
import scalaz.std.option.optionInstance
import scalaz.syntax.applicative.ToApplyOps

package object model {
  type SystemName = String
  type ClusterNodeName = String
  type DependencyLabel = String

  type VisibilityNode = Visibility[Node]
  type VisibilityDependency = Visibility[Dependency]
}