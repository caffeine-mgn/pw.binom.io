package pw.binom.metric.prometheus

import pw.binom.io.AsyncWriter

class AsyncMetricWriter(val writer: AsyncWriter) : AsyncMetricVisitor {
  private val w = InternalPrometheusWriter()

  override suspend fun start(name: String) {
    w.start(name) { writer.append(it) }
  }

  override suspend fun help(text: String) {
    w.help(text) { writer.append(it) }
  }

  override suspend fun type(text: String) {
    w.type(text) { writer.append(it) }
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
