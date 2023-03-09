package pw.binom.metric.prometheus

import pw.binom.io.AsyncAppendable
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.use
import pw.binom.metric.MetricProvider

abstract class AbstractPrometheusHandler : HttpHandler {
    protected abstract suspend fun getMetrics(): List<MetricProvider>

    protected open suspend fun getDefaultFields(): Map<String, String> = emptyMap()

    private suspend fun makeResponse(writer: AsyncAppendable) {
        val metrics = getMetrics()
        val defaultFields = getDefaultFields()
        var first = true
        metrics.forEach { provider ->
            if (!first) {
                writer.append("\n")
            }
            PrometheusResponseGenerator.generate(metric = provider, dest = writer, defaultFields = defaultFields)
            if (provider.metrics.isNotEmpty()) {
                first = false
            }
        }
    }

    override suspend fun handle(exchange: HttpServerExchange) {
        exchange.startResponse(200)
        exchange.output.bufferedAsciiWriter().use {
            makeResponse(it)
        }
    }
}
