package pw.binom.metric

interface GaugeDouble : Gauge {
  companion object {
    fun create(
      name: String,
      description: String? = null,
      fields: Map<String, String> = emptyMap(),
      valueProvider: () -> Double,
    ): GaugeDouble = object : GaugeDouble {
      override val value: Double
        get() = valueProvider()
      override val fields: Map<String, String> = fields
      override val name: String = name
      override val description: String? = description
    }
  }

  val value: Double
}
