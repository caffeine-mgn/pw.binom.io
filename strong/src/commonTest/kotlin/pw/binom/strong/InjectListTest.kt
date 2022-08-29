package pw.binom.strong

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InjectListTest {

    interface Service
    class A : Service
    class B : Service

    @Test
    fun test() = runTest {
        val strong = Strong.create(
            Strong.config {
                it.bean { A() }
                it.bean { B() }
            }
        )

        val b by strong.injectServiceList<Service>()
        assertEquals(2, b.size)
        assertTrue(b.any { it is A })
        assertTrue(b.any { it is B })
    }
}
