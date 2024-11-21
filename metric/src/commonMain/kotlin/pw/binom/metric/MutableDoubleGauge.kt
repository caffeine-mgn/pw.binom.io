package pw.binom.metric

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicDouble
import pw.binom.atomic.synchronize
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableDoubleGauge(
  override val name: String,
  override val description: String? = null,
  override val fields: Map<String, String> = emptyMap(),
) : GaugeDouble, ReadWriteProperty<Any?, Double> {
  private val _value = AtomicDouble(0.0)
  private val lock = AtomicBoolean(false)
  override var value: Double
    get() = _value.getValue()
    set(value) {
      lock.synchronize {
        _value.setValue(value)
      }
    }

  override fun getValue(thisRef: Any?, property: KProperty<*>): Double = value
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
    this.value = value
  }

  fun inc() = inc(1.0)

  fun inc(value: Double) = lock.synchronize {
    val oldValue = _value.getValue()
    val newValue = oldValue + value
    _value.setValue(newValue)
    newValue
  }

  fun dec() = dec(1.0)
  fun dec(value: Double) = inc(-value)

  fun set(value: Double) {
    lock.synchronize {
      this.value = value
    }
  }
}
