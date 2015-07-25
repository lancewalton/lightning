package lightning.evaluator.json

import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.shaka.http.Status
import lightning.model.SystemName
import lightning.model.Node

case class HttpGraphStatusEvaluator(url: String, targetNode: Node) extends JsonStatusEvaluator {
  def apply() = {
    val response = http(GET(url))
    if (response.status == Status.OK)
      SuccessfulStatusResponse(url, targetNode, response.entityAsString)
    else 
      UnsuccessfulStatusResponse(url, targetNode)
  }
}