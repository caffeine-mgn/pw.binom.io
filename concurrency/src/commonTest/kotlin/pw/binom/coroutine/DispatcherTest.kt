package pw.binom.coroutine

import pw.binom.concurrency.suspendManagedCoroutine
import pw.binom.network.NetworkDispatcher
import pw.binom.neverFreeze
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatcherTest {
    class MyClass{
        var i = 0
    }
    @Test
    fun dd(){
        val nd = NetworkDispatcher()
        val c = MyClass()
        c.neverFreeze()
        nd.runSingle {
            suspendManagedCoroutine<Int> {
                c.i++
                it.resume(0)
            }
        }
        assertEquals(1,c.i)
    }
}