package pw.binom.metric

import pw.binom.atomic.AtomicDouble

class MutableGauge(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : Gauge {
    private var _value = AtomicDouble(0.0)
    override var value: Double
        get() = _value.getValue()
        set(value) {
            _value.setValue(value)
        }

    fun inc(value: Double = 1.0) {
        this.value += value
    }

    fun dec(value: Double = 1.0) {
        this.value -= value
    }

    fun set(value: Double) {
        this.value = value
    }
}
