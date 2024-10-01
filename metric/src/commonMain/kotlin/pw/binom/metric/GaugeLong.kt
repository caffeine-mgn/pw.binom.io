package pw.binom.metric

interface GaugeLong : Gauge {
  companion object {
    fun create(
      name: String,
      description: String? = null,
      fields: Map<String, String> = emptyMap(),
      valueProvider: () -> Long,
    ): GaugeLong = object : GaugeLong {
      override val value: Long
        get() = valueProvider()
      override val fields: Map<String, String> = fields
      override val name: String = name
      override val description: String? = description
    }
  }

  val value: Long
}
