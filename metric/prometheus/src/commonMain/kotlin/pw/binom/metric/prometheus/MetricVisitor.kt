package pw.binom.metric.prometheus

interface MetricVisitor {
  fun start(name: String)
  fun help(text: String)
  fun type(text: String)
  fun field(name: String, value: String)
  fun value(value: String)
  fun end()

  fun toAsync() = object : AsyncMetricVisitor {
    override suspend fun start(name: String) {
      this@MetricVisitor.start(name)
    }

    override suspend fun help(text: String) {
      this@MetricVisitor.help(text)
    }

    override suspend fun type(text: String) {
      this@MetricVisitor.type(text)
    }

    override suspend fun field(name: String, value: String) {
      this@MetricVisitor.field(name = name, value = value)
    }

    override suspend fun value(value: String) {
      this@MetricVisitor.value(value)
    }

    override suspend fun end() {
      this@MetricVisitor.end()
    }
  }
}
