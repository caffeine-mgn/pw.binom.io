package pw.binom.metric.prometheus

import pw.binom.metric.MetricType

internal class InternalPrometheusWriter {
  private var fieldStart = false

  inline fun start(name: String, writer: (String) -> Unit) {
    fieldStart = false
    writer(name)
  }

  inline fun help(name:String, text: String, writer: (String) -> Unit) {
    writer("# HELP ")
    writer(name)
    writer(" ")
    writer(text)
    writer("\n")
  }

  inline fun type(name:String, type: MetricType, writer: (String) -> Unit) {
    writer("# TYPE ")
    writer(name)
    writer(" ")
    val typeStr = when (type) {
      is MetricType.COUNTER -> "counter"
      is MetricType.GAUGE -> "gauge"
      is MetricType.Custom -> type.name
    }
    writer(typeStr)
    writer("\n")
  }

  inline fun field(name: String, value: String, writer: (String) -> Unit) {
    if (fieldStart) {
      writer(",")
    } else {
      fieldStart = true
      writer("{")
    }
    writer(name)
    writer("=\"")
    writer(value)
    writer("\"")
  }

  inline fun value(value: String, writer: (String) -> Unit) {
    if (fieldStart) {
      writer("}")
    }
    writer(" ")
    writer(value)
  }

  inline fun end(writer: (String) -> Unit) {
    writer("\n")
  }
}
