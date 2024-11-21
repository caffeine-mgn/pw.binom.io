package pw.binom.metric

internal class WithFieldAsyncMetricVisitor(
  val name: String,
  val value: String,
  val visitor: AsyncMetricVisitor,
) : AsyncMetricVisitor {
  override suspend fun end() {
    visitor.end()
  }

  override suspend fun field(name: String, value: String) {
    if (name == this.name) {
      return
    }
    visitor.field(name, value)
  }

  override suspend fun help(name: String, text: String) {
    visitor.help(name = name, text = text)
  }

  override suspend fun start(name: String) {
    visitor.start(name)
  }

  override suspend fun type(name: String, type: MetricType) {
    visitor.type(name = name, type = type)
  }

  override suspend fun value(value: String) {
    visitor.field(name = this.name, value = this.value)
    visitor.value(value)
  }
}
