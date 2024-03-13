package pw.binom.strong.web.server

import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.strong.injectServiceList
import pw.binom.url.toPath

class HealthController : ManagementHttpHandler {
  private val healthIndicators by injectServiceList<HealthIndicator>()

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET") {
      return
    }
    if (exchange.requestURI.path != "/health".toPath) {
      return
    }
    val resp = exchange.response()
    val isNotHealthy = healthIndicators.any { !it.isHealthy() }
    if (isNotHealthy) {
      resp.status = 523
      resp.send("NOT HEALTHY")
    } else {
      resp.status = 200
      resp.send("OK")
    }
  }
}
