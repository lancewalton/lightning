package lightning.evaluator.decorator

import scala.concurrent.duration.Duration
import lightning.evaluator.StatusEvaluator
import java.util.Date
import lightning.evaluator.StatusResponse

case class CachingStatusEvaluator(underlying: StatusEvaluator, timeout: Duration) extends StatusEvaluator {
  private var latestResponse: Option[StatusResponse] = None
  private var latestTime: Option[Long] = None
  
  def apply = {
    val (lto, lro) = this.synchronized { (latestTime, latestResponse) }
    (for {
      lt <- lto
      if System.currentTimeMillis - lt < timeout.toMillis
      lr <- lro
    } yield lr) getOrElse delegate
  }
  
  private def delegate() = {
    val response = underlying()
    val time = System.currentTimeMillis
    this.synchronized {
      latestResponse = Some(response)
      latestTime = Some(time)
    }
    response
  }
}