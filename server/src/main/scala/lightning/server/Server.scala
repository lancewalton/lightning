package lightning.server

import scala.util.Properties
import scalaz.NonEmptyList
import scalaz.Validation
import lightning.configuration.loader.FallbackConfigurationLoader

object Server extends App {

  private def loadConfiguration() = {
    lazy val config: String = Properties.propOrNone("config").getOrElse {
      val err =
        s"""
         |No config was specified so the system will shutdown.
         |
         |Please set system property config as follows:
         |
         |  -Dconfig=local
         |
         |Where 'local' should reflect the configuration for your environment.
         |Ask the Boost team for help if you have questions.
         |
         |Have a nice day.
       """.stripMargin
      println(err)
      System.exit(1)
      throw new RuntimeException(err)
    }

    Validation.fromTryCatchNonFatal(FallbackConfigurationLoader.load(config)).fold(shutdownErr, identity)
  }

  private def shutdownErr(err: Throwable): Nothing = {
    err.printStackTrace()
    die()
  }

  def shutdown(err: NonEmptyList[String]): Nothing = {
    val errors: String = err.list.mkString("\n")
    println(errors)
    die()
  }

  private def die(): Nothing = sys.exit(-1)
}