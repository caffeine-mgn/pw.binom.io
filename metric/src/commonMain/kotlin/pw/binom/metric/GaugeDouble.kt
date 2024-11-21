package pw.binom.metric

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface GaugeDouble : Gauge<Double>, ReadOnlyProperty<Any?, Double> {
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
      override fun getValue(thisRef: Any?, property: KProperty<*>): Double = value
    }
  }

  val value: Double

  override fun getValue(thisRef: Any?, property: KProperty<*>): Double = value

  override suspend fun accept(visitor: AsyncMetricVisitor) {
    description?.let { visitor.help(name = name, text = it) }
    visitor.type(name = name, type = MetricType.GAUGE)
    visitor.start(name = name)
    fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(value.toString())
    visitor.end()
  }

  override fun accept(visitor: MetricVisitor) {
    description?.let { visitor.help(name = name, text = it) }
    visitor.type(name = name, type = MetricType.GAUGE)
    visitor.start(name = name)
    fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(value.toString())
    visitor.end()
  }
}
