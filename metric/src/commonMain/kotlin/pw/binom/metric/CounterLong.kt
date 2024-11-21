package pw.binom.metric

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface CounterLong : Counter, ReadOnlyProperty<Any?, Long> {
  val value: Long

  override fun getValue(thisRef: Any?, property: KProperty<*>): Long = value

  override suspend fun accept(visitor: AsyncMetricVisitor) {
    description?.let { visitor.help(name = name, text = it) }
    visitor.type(name = name, type = MetricType.COUNTER)
    visitor.start(name = name)
    fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(value.toString())
    visitor.end()
  }

  override fun accept(visitor: MetricVisitor) {
    description?.let { visitor.help(name = name, text = it) }
    visitor.type(name = name, type = MetricType.COUNTER)
    visitor.start(name = name)
    fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(value.toString())
    visitor.end()
  }
}
