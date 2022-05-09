package pw.binom.metric

import pw.binom.atomic.AtomicDouble

class Gauge(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : MetricUnit {
    var value by AtomicDouble(0.0)
        private set

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
