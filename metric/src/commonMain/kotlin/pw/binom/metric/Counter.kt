package pw.binom.metric

import pw.binom.atomic.AtomicDouble

class Counter(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : MetricUnit {
    private var _value = AtomicDouble(0.0)
    var value: Double
        get() = _value.getValue()
        set(value) {
            _value.setValue(value)
        }

    fun inc(value: Double = 1.0) {
        this.value += value
    }
}
