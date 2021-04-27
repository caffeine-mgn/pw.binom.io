package pw.binom

import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.Lock
import pw.binom.concurrency.Worker
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.seconds

class FinalizerTest {
    var out = AtomicReference<Any?>(null)

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        val f = Finalizer()
        var aa:Lock? = Lock()
        out.value = aa!!.doFreeze()

        f.defineFinalize(aa!!) {
            println("Removed!")
        }

//        Worker().execute(out) {
//            it.value = null
//        }
        val now = TimeSource.Monotonic.markNow()
        while (now.elapsedNow() < 10.0.seconds) {
            System.gc()
            f.forceCleanup()
            aa = null
            out.value = null
        }
    }
}