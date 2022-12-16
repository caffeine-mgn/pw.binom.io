package pw.binom.metric

import pw.binom.atomic.AtomicLong

class MutableLongGauge(
    override val name: String,
    override val description: String? = null,
    override val fields: Map<String, String> = emptyMap(),
) : GaugeLong {
    private var _value = AtomicLong(0)
    override var value: Long
        get() = _value.getValue()
        set(value) {
            _value.setValue(value)
        }

    fun inc() = inc(1L)
    fun dec() = dec(1L)

    fun inc(value: Long) = this._value.addAndGet(value)
    fun dec(value: Long) = inc(-value)

    fun inc(value: Int) = inc(value.toLong())
    fun dec(value: Int) = dec(value.toLong())

    fun set(value: Long) {
        this._value.setValue(value)
    }
}
