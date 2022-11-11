package pw.binom.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAverageGaugeTest {
    @Test
    fun test() {
        val avr = DoubleRollingAverageGauge(windowSize = 3, name = "")
        assertEquals(0, avr.cursor)
        assertEquals(0, avr.size)

        avr.put(1.0)
        assertEquals(1, avr.cursor)
        assertEquals(1, avr.size)

        avr.put(2.0)
        assertEquals(2, avr.cursor)
        assertEquals(2, avr.size)

        avr.put(3.0)
        assertEquals(1, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(4.0)
        assertEquals(0, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(5.0)
        assertEquals(1, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(6.0)
        assertEquals(2, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(7.0)
        assertEquals(1, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(8.0)
        assertEquals(0, avr.cursor)
        assertEquals(3, avr.size)

        avr.put(9.0)
        assertEquals(1, avr.cursor)
        assertEquals(3, avr.size)
    }
}
