package pw.binom.metric.prometheus

import pw.binom.io.AsyncAppendable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.metric.MetricProvider

abstract class AbstractPrometheusHandler : Handler {
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

    override suspend fun request(req: HttpRequest) {
        req.response {
            it.status = 200
            it.sendText {
                makeResponse(it)
            }
        }
    }
}
