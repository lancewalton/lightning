package lightning.model

import java.util.Date

import argonaut.Argonaut.{ OptionDecodeJson, OptionEncodeJson, casecodec2 }
import argonaut.CodecJson
import scalaz.Equal
import scalaz.Equal.equalA

case class Timestamped[T](timestamped: T, timestamp: Option[Date])

object Timestamped {
  implicit def codec[T: CodecJson]() = casecodec2(Timestamped.apply[T], Timestamped.unapply[T])("timestamped", "timestamp")

  implicit def equal[T: Equal]() = equalA[Timestamped[T]]
}