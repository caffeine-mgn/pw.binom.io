package pw.binom.metric

internal class ModifyAsyncMetricVisitor(
  val visitor: AsyncMetricVisitor,
) : AsyncMetricVisitor {
  val fields = HashMap<String, String>()
  var prefix: String = ""


  override suspend fun end() {
    visitor.end()
  }

  override suspend fun field(name: String, value: String) {
    if (fields.containsKey(name)) {
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
    fields.forEach { (key, value) ->
      visitor.field(name = key, value = value)
    }
    visitor.value(value)
  }
}
