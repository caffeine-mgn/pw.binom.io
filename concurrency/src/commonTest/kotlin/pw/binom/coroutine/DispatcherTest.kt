package pw.binom.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.neverFreeze
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatcherTest {
    class MyClass {
        var i = 0
    }

    @Test
    fun dd() = runBlocking {
        val c = MyClass()
        c.neverFreeze()
        GlobalScope.launch {
            suspendCancellableCoroutine<Int> {
                c.i++
                it.resume(0, null)
            }
        }.join()
        assertEquals(1, c.i)
    }
}
