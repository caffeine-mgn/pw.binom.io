package pw.binom.metric

sealed interface MetricUnit : Metric {
  val fields: Map<String, String>
  val name: String
  val description: String?
}
