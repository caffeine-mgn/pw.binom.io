package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AtomicDouble(value: Double) : ReadWriteProperty<Any?, Double> {
    private val body = AtomicLong(value.toRawBits())

    fun compareAndSet(expected: Double, new: Double): Boolean =
        body.compareAndSet(expected = expected.toRawBits(), new = new.toRawBits())

    override fun getValue(thisRef: Any?, property: KProperty<*>): Double =
        Double.fromBits(body.getValue(thisRef, property))

    var value: Double
        get() = Double.fromBits(body.value)
        set(value) {
            body.value = value.toRawBits()
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        body.setValue(thisRef, property, value.toRawBits())
    }
}