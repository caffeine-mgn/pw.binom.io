package pw.binom.metric

internal class WithFieldSyncMetricVisitor(
  val nameModification: String,
  val value: String,
  val visitor: MetricVisitor,
) : MetricVisitor {
  override fun end() {
    visitor.end()
  }

  override fun field(name: String, value: String) {
    if (name == this.nameModification) {
      return
    }
    visitor.field(name, value)
  }

  override fun help(name: String, text: String) {
    visitor.help(name = name, text = text)
  }

  override fun start(name: String) {
    visitor.start(name)
  }

  override fun type(name: String, type: MetricType) {
    visitor.type(name = name, type = type)
  }

  override fun value(value: String) {
    visitor.field(name = this.nameModification, value = this.value)
    visitor.value(value)
  }
}
