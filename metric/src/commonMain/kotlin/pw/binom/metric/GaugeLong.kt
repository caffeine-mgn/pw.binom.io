package pw.binom.metric

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface GaugeLong : Gauge<Long>, ReadOnlyProperty<Any?, Long> {
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

  override fun getValue(thisRef: Any?, property: KProperty<*>): Long = value

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
