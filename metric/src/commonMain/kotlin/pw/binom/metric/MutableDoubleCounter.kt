package pw.binom.metric

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicDouble
import pw.binom.atomic.synchronize

class MutableDoubleCounter(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : CounterDouble {
    private val _value = AtomicDouble(0.0)
    private val lock = AtomicBoolean(false)
    override val value: Double
        get() = _value.getValue()

    fun inc() = inc(1.0)
    fun inc(value: Double): Double {
        require(value >= 0.0) { "value should be more or equals 0" }
        return lock.synchronize {
            val old = _value.getValue()
            val newValue = old + value
            _value.setValue(newValue)
            newValue
        }
    }
}
