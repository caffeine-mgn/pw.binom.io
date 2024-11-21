package pw.binom.metric

interface MetricVisitor {
  fun start(name: String)
  fun help(name: String, text: String)
  fun type(name: String, type: MetricType)
  fun field(name: String, value: String)
  fun value(value: String)
  fun end()

  fun toAsync() = object : AsyncMetricVisitor {
    override suspend fun start(name: String) {
      this@MetricVisitor.start(name)
    }

    override suspend fun help(metricName: String, text: String) {
      this@MetricVisitor.help(name = metricName, text = text)
    }

    override suspend fun type(metricName: String, type: MetricType) {
      this@MetricVisitor.type(name = metricName, type = type)
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

  fun withField(nameModification: String, value: String): MetricVisitor = WithFieldSyncMetricVisitor(
    nameModification = nameModification,
    value = value,
    visitor = this,
  )

  fun changeName(nameModification: (String) -> String): MetricVisitor = PrefixSyncMetricVisitor(
    nameModification = nameModification,
    visitor = this@MetricVisitor
  )
}
