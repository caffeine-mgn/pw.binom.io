package pw.binom.strong.web.server.controllers

import kotlinx.coroutines.withTimeoutOrNull
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.strong.HealthIndicator
import pw.binom.strong.injectServiceList
import pw.binom.strong.web.server.ManagementHttpHandler
import pw.binom.url.toPath
import kotlin.time.Duration.Companion.seconds

class LivenessController : ManagementHttpHandler {

  private val healthIndicators by injectServiceList<HealthIndicator>()
  private val logger by Logger.ofThisOrGlobal

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET") {
      return
    }
    if (exchange.requestURI.path != "/health/liveness".toPath && exchange.requestURI.path != "/health".toPath) {
      return
    }
//readiness
    val isNotHealthy = healthIndicators.filter {
      withTimeoutOrNull(1.seconds) { !it.isHealthy() } != false
    }
    exchange.response {
      if (isNotHealthy.isNotEmpty()) {
        logger.info("Unhealthy: ${isNotHealthy.map { it.componentName ?: it::class.qualifiedName ?: it::class.simpleName ?: it::class.toString() }}")
        status = 523
        send("NOT HEALTHY")
      } else {
        status = 200
        send("OK")
      }
    }
  }
}
