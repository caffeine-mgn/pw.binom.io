package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkerTest {
    @Test
    fun test() {
        val w = Worker()
        val r = Random.nextInt()
        val r2 = w.execute(r) {
            r + 1
        }.resultOrNull!!

        assertEquals(r + 1, r2)
    }

    @Test
    fun test2() {
        val w = Worker()
        var r = AtomicInt(Random.nextInt())
        val r2 = w.execute(Unit) {
            r.value + 1
        }.resultOrNull!!

        assertEquals(r.value + 1, r2)
    }
}