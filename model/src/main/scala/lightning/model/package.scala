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

  type TimestampedNode = Timestamped[Node]
  type TimestampedDependency = Timestamped[Dependency]

  private def dateFormat() = new SimpleDateFormat("yyyyMMddkkmmssSSSX")

  private def parseAsDate(s: String) = \/.fromTryCatchNonFatal {
    dateFormat.parse(s)
  } leftMap { t ⇒ s"Can't parse '$s' as a datetime because: " + t.getMessage }

  implicit val dateCodec =
    CodecJson[Date](
      d ⇒ StringEncodeJson.encode(dateFormat.format(d)),
      hc ⇒ hc.as[String].flatMap { parseAsDate(_).bimap(f ⇒ DecodeResult.fail[Date](f, hc.history), DecodeResult.ok(_)).fold(identity, identity) })

  implicit class OptionSyntax[T](lhs: Option[T]) {
    def select(rhs: Option[T])(f: (T, T) ⇒ T): Option[T] = (lhs |@| rhs)(Tuple2.apply _).map { case (l, r) ⇒ f(l, r) } orElse lhs orElse rhs
  }
}