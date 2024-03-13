package pw.binom.strong.web.server

import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.metric.MetricProvider
import pw.binom.metric.prometheus.AbstractPrometheusHandler
import pw.binom.strong.injectServiceList
import pw.binom.url.toURI

class MetricsController : ManagementHttpHandler {
  private val metrics by injectServiceList<MetricProvider>()

  private val prometheusHandler =
    object : AbstractPrometheusHandler() {
      override suspend fun getMetrics(): List<MetricProvider> = metrics
    }

  override suspend fun handle(exchange: HttpServerExchange) {
    if (exchange.requestMethod != "GET" || exchange.requestURI != "/metrics".toURI()) {
      return
    }
    prometheusHandler.handle(exchange)
  }
}
