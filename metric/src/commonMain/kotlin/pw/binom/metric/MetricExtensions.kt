package pw.binom.metric

fun Metric.wrap(
  sync: (MetricVisitor) -> MetricVisitor,
  async: (AsyncMetricVisitor) -> AsyncMetricVisitor,
) =
  object : Metric {
    override fun accept(visitor: MetricVisitor) {
      this@wrap.accept(sync(visitor))
    }

    override suspend fun accept(visitor: AsyncMetricVisitor) {
      this@wrap.accept(async(visitor))
    }
  }

/**
 * Add field [name] with [value] to all fields of `receiver`
 */
fun Metric.withField(
  name: String,
  value: String,
) = wrap(
  sync = { it.withField(nameModification = name, value = value) },
  async = { it.withField(name = name, value = value) }
)

fun metricOf(metrics: List<Metric>) = object : Metric {
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

fun Metric.withPrefix(
  prefix: String,
) = changeName { original ->
  "$prefix$original"
}

fun Metric.removePrefix(
  prefix: String,
) = changeName { original ->
  original.removePrefix(prefix)
}

fun Metric.removeSuffix(
  suffix: String,
) = changeName { original ->
  original.removeSuffix(suffix)
}

fun Metric.changeName(
  nameModification: (String) -> String,
) = wrap(
  sync = { it.changeName(nameModification = nameModification) },
  async = { it.changeName(nameModification = nameModification) }
)
