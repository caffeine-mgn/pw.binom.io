package pw.binom.thread

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import kotlin.test.*

@Ignore
class ThreadTest {

    @Test
    fun noFrostTest() {
        var vv = AtomicInt(0)
        val w = Worker()
        w.execute(vv) {
            it.increment()
        }
        Worker.sleep(1000)
        assertEquals(1, vv.value)
    }

    @Test
    fun currentThreadTest() {
        val mainThread = Worker.current
        val newThread = Worker()
        var done = AtomicBoolean(false)
        newThread.execute(done) {
            assertNotEquals(Worker.current?.id, mainThread?.id)
            done.value = true
        }
        Worker.sleep(1000)
        assertTrue(done.value)
    }
}