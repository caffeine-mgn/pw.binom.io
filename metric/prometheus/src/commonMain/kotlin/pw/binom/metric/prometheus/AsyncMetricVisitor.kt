package pw.binom.metric.prometheus

interface AsyncMetricVisitor {
  suspend fun start(name:String)
  suspend fun help(text: String)
  suspend fun type(text: String)
  suspend fun field(name: String, value: String)
  suspend fun value(value: String)
  suspend fun end()
}
