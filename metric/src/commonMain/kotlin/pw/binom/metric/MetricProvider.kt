package pw.binom.metric

interface MetricProvider : Metric {
  val metrics: List<MetricUnit>

  override fun accept(visitor: MetricVisitor) {
    metrics.forEach {
      it.accept(visitor)
    }
  }

  override suspend fun accept(visitor: AsyncMetricVisitor) {
    metrics.forEach {
      it.accept(visitor)
    }
  }
}
