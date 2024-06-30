package pw.binom.metric.prometheus

internal class InternalPrometheusWriter {
  private var fieldStart = false

  inline fun start(name: String, writer: (String) -> Unit) {
    fieldStart = false
    writer(name)
  }

  inline fun help(text: String, writer: (String) -> Unit) {
    writer("# HELP ")
    writer(text)
    writer("\n")
  }

  inline fun type(text: String, writer: (String) -> Unit) {
    writer("# TYPE ")
    writer(text)
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
