package lightning.evaluator

import lightning.model.Node
import io.shaka.http.Http.http
import io.shaka.http.Request.GET
import io.shaka.http.Status
import lightning.model.SystemName
import lightning.model.Graph
import java.util.Date
import lightning.model.Timestamped

case class HttpOkStatusEvaluator(url: String, targetNode: Node) extends StatusEvaluator {
  def apply() = {
    val response = http(GET(url))
    if (response.status == Status.OK)
      SuccessfulStatusResponse(url, RootedGraph(Graph.empty ⊕ Timestamped(targetNode, Option(new Date)), targetNode))
    else
      UnsuccessfulStatusResponse(url, targetNode)
  }
}