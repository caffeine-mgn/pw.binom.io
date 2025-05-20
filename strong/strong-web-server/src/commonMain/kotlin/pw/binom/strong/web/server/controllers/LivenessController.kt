package pw.binom.strong.web.server.controllers

import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.strong.HealthIndicator
import pw.binom.strong.injectServiceList
import pw.binom.strong.web.server.ManagementHttpHandler
import pw.binom.url.toPath

class LivenessController : ManagementHttpHandler {

  private val healthIndicators by injectServiceList<HealthIndicator>()

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET") {
      return
    }
    if (exchange.requestURI.path != "/health/liveness".toPath && exchange.requestURI.path != "/health".toPath) {
      return
    }
//readiness
    val isNotHealthy = healthIndicators.any { !it.isHealthy() }
    exchange.response {
      if (isNotHealthy) {
        status = 523
        send("NOT HEALTHY")
      } else {
        status = 200
        send("OK")
      }
    }
  }
}
