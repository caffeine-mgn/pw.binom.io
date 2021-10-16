package pw.binom.atomic

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AtomicBooleanTest {
    @Test
    fun compareAndSetTest() {
        val f = AtomicBoolean(false)

        assertTrue(f.compareAndSet(false, true))
        assertFalse(f.compareAndSet(false, true))
        assertTrue(f.compareAndSet(true, false))
    }
}