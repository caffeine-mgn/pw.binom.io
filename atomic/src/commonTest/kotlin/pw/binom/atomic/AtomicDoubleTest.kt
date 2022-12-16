package pw.binom.atomic

import kotlin.test.Test
import kotlin.test.assertEquals

class AtomicDoubleTest {

    fun AtomicDouble.inc(value: Double = 1.0) {
        setValue(getValue() + value)
    }

    fun AtomicDouble.dec(value: Double = 1.0) {
        inc(-value)
    }

    @Test
    fun incDecTest() {
        val v = AtomicDouble(0.0)
        v.inc()
        v.inc()
        v.inc()
        v.dec()
        v.inc()
        assertEquals(3.0, v.getValue())
    }
}
