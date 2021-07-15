package pw.binom.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AtomicFloat(value: Float) : ReadWriteProperty<Any?, Float> {
    private val body = AtomicInt(value.toRawBits())

    fun compareAndSet(expected: Float, new: Float): Boolean =
        body.compareAndSet(expected = expected.toRawBits(), new = new.toRawBits())

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float =
        Float.fromBits(body.getValue(thisRef, property))

    var value: Float
        get() = Float.fromBits(body.value)
        set(value) {
            body.value = value.toRawBits()
        }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        body.setValue(thisRef, property, value.toRawBits())
    }
}