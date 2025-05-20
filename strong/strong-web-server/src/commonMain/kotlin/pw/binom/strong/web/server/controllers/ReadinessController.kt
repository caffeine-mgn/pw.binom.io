package pw.binom.strong.web.server.controllers

import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.strong.HealthIndicator
import pw.binom.strong.injectServiceList
import pw.binom.strong.web.server.ManagementHttpHandler
import pw.binom.url.toPath

class ReadinessController : ManagementHttpHandler {

  private val healthIndicators by injectServiceList<HealthIndicator>()

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET") {
      return
    }
    if (exchange.requestURI.path != "/health/readiness".toPath) {
      return
    }
    val isNotHealthy = healthIndicators.any { !it.isReady() }
    exchange.response {
      if (isNotHealthy) {
        status = 523
        send("NOT READY")
      } else {
        status = 200
        send("OK")
      }
    }
  }
}
