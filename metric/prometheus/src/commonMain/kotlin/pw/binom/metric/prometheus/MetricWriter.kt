package pw.binom.metric.prometheus

import pw.binom.io.Writer

class MetricWriter(val writer: Writer) : MetricVisitor {
  private val w = InternalPrometheusWriter()

  override fun start(name: String) {
    w.start(name) { writer.append(it) }
  }

  override fun help(text: String) {
    w.help(text) { writer.append(it) }
  }

  override fun type(text: String) {
    w.type(text) { writer.append(it) }
  }

  override fun field(name: String, value: String) {
    w.field(name = name, value = value) { writer.append(it) }
  }

  override fun value(value: String) {
    w.value(value) { writer.append(it) }
  }

  override fun end() {
    w.end { writer.append(it) }
  }
}
