package pw.binom.metric.prometheus

import pw.binom.io.AsyncAppendable
import pw.binom.metric.AsyncMetricVisitor
import pw.binom.metric.MetricType

class AsyncMetricWriter(val writer: AsyncAppendable) : AsyncMetricVisitor {
  private val w = InternalPrometheusWriter()

  override suspend fun start(name: String) {
    w.start(name) { writer.append(it) }
  }

  override suspend fun help(metricName: String, text: String) {
    w.help(metricName, text) { writer.append(it) }
  }

  override suspend fun type(metricName: String, type: MetricType) {
    w.type(metricName, type) { writer.append(it) }
  }

  override suspend fun field(name: String, value: String) {
    w.field(name = name, value = value) { writer.append(it) }
  }

  override suspend fun value(value: String) {
    w.value(value) { writer.append(it) }
  }

  override suspend fun end() {
    w.end { writer.append(it) }
  }
}
