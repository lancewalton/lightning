package lightning.model

import argonaut.Argonaut.{ OptionDecodeJson, OptionEncodeJson, casecodec2 }
import argonaut.CodecJson
import scalaz.Equal
import scalaz.Equal.equalA

case class Visibility[T](item: T, visible: Boolean)

object Visibility {
  implicit def codec[T: CodecJson]() = casecodec2(Visibility.apply[T], Visibility.unapply[T])("item", "visibility")

  implicit def equal[T: Equal]() = equalA[Visibility[T]]
}