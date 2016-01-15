package lightning.evaluator.json

import lightning.evaluator.StatusEvaluator
import lightning.model.Dependency
import argonaut.Parse
import lightning.evaluator.RootedGraph

trait JsonStatusEvaluator extends Function0[StatusResponse] {
  def asStatusEvaluator() = new StatusEvaluator {
    def apply(): lightning.evaluator.StatusResponse = transformResponse(JsonStatusEvaluator.this.apply)
  }
  
  private def transformResponse(response: StatusResponse): lightning.evaluator.StatusResponse =
    response match {
      case UnsuccessfulStatusResponse(dl, node) ⇒ lightning.evaluator.UnsuccessfulStatusResponse(dl, node)
      
      case SuccessfulStatusResponse(dl, node, js) ⇒
        (for {
          json ← Parse.parse(js).toOption
          graph ← RootedGraph.codec.decode(json.hcursor).toOption
        } yield lightning.evaluator.SuccessfulStatusResponse(dl, graph)) getOrElse {
          lightning.evaluator.ErrorStatusResponse(dl, node, "Invalid JSON returned")
        }
    }
}