package lightning.configuration

import scalaz.{ Validation, ValidationNel }

package object loader {
  type ErrorMessage = String
  type Validated[T] = ValidationNel[ErrorMessage, T]

  def toValidated[A](a: => A): Validated[A] = Validation.fromTryCatchNonFatal(a).leftMap(_.toString).toValidationNel
}