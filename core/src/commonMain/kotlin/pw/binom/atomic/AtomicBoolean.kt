package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AtomicBoolean(value: Boolean) : ReadWriteProperty<Any?, Boolean> {
    private val atom = AtomicInt(boolToInt(value))
    fun compareAndSet(expected: Boolean, new: Boolean): Boolean =
            atom.compareAndSet(boolToInt(expected), boolToInt(new))

    private fun boolToInt(value: Boolean) = if (value) 1 else 0

    var value: Boolean
        get() = atom.value == 1
        set(value: Boolean) {
            atom.value = boolToInt(value)
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        this.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = this.value
}