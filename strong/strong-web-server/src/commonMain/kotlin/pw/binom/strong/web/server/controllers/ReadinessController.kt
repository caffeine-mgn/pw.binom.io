package pw.binom.strong.web.server.controllers

import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.strong.HealthIndicator
import pw.binom.strong.injectServiceList
import pw.binom.strong.web.server.ManagementHttpHandler
import pw.binom.url.toPath

class ReadinessController : ManagementHttpHandler {

  private val healthIndicators by injectServiceList<HealthIndicator>()
  private val logger by Logger.ofThisOrGlobal

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET") {
      return
    }
    if (exchange.requestURI.path != "/health/readiness".toPath) {
      return
    }
    val isNotHealthy = healthIndicators.filter { !it.isReady() }
    exchange.response {
      if (isNotHealthy.isNotEmpty()) {
        logger.info("Not ready: ${isNotHealthy.map { it.componentName ?: it::class.qualifiedName ?: it::class.simpleName ?: it::class.toString() }}")
        status = 523
        send("NOT READY")
      } else {
        status = 200
        send("OK")
      }
    }
  }
}
