package pw.binom.metric.prometheus

import pw.binom.metric.MetricType
import pw.binom.metric.MetricVisitor

class MetricWriter(val writer: Appendable) : MetricVisitor {
  private val w = InternalPrometheusWriter()

  override fun start(name: String) {
    w.start(name) { writer.append(it) }
  }

  override fun help(name: String, text: String) {
    w.help(name = name, text = text) { writer.append(it) }
  }

  override fun type(name: String, type: MetricType) {
    w.type(name = name, type = type) { writer.append(it) }
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
