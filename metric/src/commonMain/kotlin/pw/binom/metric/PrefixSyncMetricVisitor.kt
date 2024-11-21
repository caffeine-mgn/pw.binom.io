package pw.binom.metric

internal class PrefixSyncMetricVisitor(
  val nameModification: (String) -> String,
  val visitor: MetricVisitor,
) : MetricVisitor {
  override fun end() {
    visitor.end()
  }

  override fun field(name: String, value: String) {
    visitor.field(name, value)
  }

  override fun help(name: String, text: String) {
    visitor.help(name = name, text = text)
  }

  override fun start(name: String) {
    val newName = nameModification(name)
    visitor.start(newName)
  }

  override fun type(name: String, type: MetricType) {
    visitor.type(name = name, type = type)
  }

  override fun value(value: String) {
    visitor.value(value)
  }
}
