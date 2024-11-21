package pw.binom.metric

sealed interface MetricType {
  val name: String

  object COUNTER : MetricType {
    override val name: String
      get() = "counter"

  }

  object GAUGE : MetricType {
    override val name: String
      get() = "gauge"

  }

  open class Custom(override val name: String) : MetricType
}
