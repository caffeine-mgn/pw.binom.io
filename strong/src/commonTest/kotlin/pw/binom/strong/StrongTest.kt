package pw.binom.strong

import pw.binom.async2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrongTest {

    interface A
    interface B
    class AImpl : A
    class BImpl : B
    class ABImpl : A, B

    @Test
    fun serviceList() {
        asyncTest {
            val s = Strong.create(Strong.config {
                it.define(AImpl())
                it.define(BImpl())
                it.define(ABImpl())
            })
            s.start()
            val list by s.serviceList<A>()
            assertEquals(2, list.size)
            assertTrue(list.any { it is AImpl })
            assertTrue(list.any { it is ABImpl })
        }
    }
}