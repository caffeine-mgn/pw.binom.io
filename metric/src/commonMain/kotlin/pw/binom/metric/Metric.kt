package pw.binom.metric

interface Metric {
  fun accept(visitor: MetricVisitor)
  suspend fun accept(visitor: AsyncMetricVisitor)
}
