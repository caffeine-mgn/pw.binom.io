package pw.binom.metric

import pw.binom.atomic.AtomicLong

class MutableLongCounter(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : CounterLong {
    private var _value = AtomicLong(0)
    override val value: Long
        get() = _value.getValue()

    fun inc(value: Long): Long {
        require(value >= 0) { "value should be more or equals 0" }
        return _value.addAndGet(value)
    }

    fun inc() = inc(1L)
}
