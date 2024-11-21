package pw.binom.metric

internal class PrefixAsyncMetricVisitor(
  val nameModification: (String) -> String,
  val visitor: AsyncMetricVisitor,
) : AsyncMetricVisitor {
  override suspend fun end() {
    visitor.end()
  }

  override suspend fun field(name: String, value: String) {
    visitor.field(name, value)
  }

  override suspend fun help(name: String, text: String) {
    visitor.help(name = name, text = text)
  }

  override suspend fun start(name: String) {
    val newName = nameModification(name)
    visitor.start(newName)
  }

  override suspend fun type(name: String, type: MetricType) {
    visitor.type(name = name, type = type)
  }

  override suspend fun value(value: String) {
    visitor.value(value)
  }
}
