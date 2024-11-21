package pw.binom.metric.prometheus

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncAppendable
import pw.binom.io.ByteBuffer
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.metric.AsyncMetricVisitor
import pw.binom.metric.Metric
import pw.binom.metric.MetricProvider

abstract class AbstractPrometheusHandler : HttpHandler {
  protected abstract suspend fun getMetrics(): List<Metric>

  protected open suspend fun getDefaultFields(): Map<String, String> = emptyMap()

  private suspend fun makeResponse(writer: AsyncAppendable) {
    val metrics = getMetrics()
    var visitor: AsyncMetricVisitor = AsyncMetricWriter(writer = writer)
    val defaultFields = getDefaultFields()
    if (defaultFields.isNotEmpty()) {
      defaultFields.forEach { (name, value) ->
        visitor = visitor.withField(name = name, value = value)
      }
    }
    metrics.forEach {
      it.accept(visitor)
    }
//    var first = true
//    metrics.forEach { provider ->
//      if (!first) {
//        writer.append("\n")
//      }
//      PrometheusResponseGenerator.generate(metric = provider, dest = writer, defaultFields = defaultFields)
//      if (provider.metrics.isNotEmpty()) {
//        first = false
//      }
//    }
  }

  protected open suspend fun usingBuffer(func: suspend (ByteBuffer) -> Unit) {
    ByteBuffer(DEFAULT_BUFFER_SIZE).use { buffer ->
      func(buffer)
    }
  }

  override suspend fun handle(exchange: HttpServerExchange) {
    exchange.startResponse(200)
    usingBuffer { buffer ->
      exchange.output.bufferedAsciiWriter().useAsync {
        makeResponse(it)
      }
    }
  }
}
