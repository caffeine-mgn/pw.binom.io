package pw.binom.concurrency

import pw.binom.coroutine.crossThreadGenerator
import pw.binom.coroutine.start
import pw.binom.network.NetworkDispatcher
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.*

class CrossThreadGeneratorTest {
    @Test
    fun test() {
        val nd = NetworkDispatcher()
        val pool = WorkerPool()
        nd.runSingle {
            val g = pool.start {
                crossThreadGenerator<Int> {
                    yield(10)
                    yield(20)
                    30
                }
            }
            assertFalse(g.isFinished)
            assertEquals(10, g.next())
            assertFalse(g.isFinished)
            assertEquals(20, g.next())
            assertFalse(g.isFinished)
            assertEquals(30, g.next())
            assertTrue(g.isFinished)
        }
        pool.shutdown()
        nd.close()
    }

    @Test
    fun exceptionTest() {
        val nd = NetworkDispatcher()
        val pool = WorkerPool()
        val errText = Random.nextUuid().toShortString()
        nd.runSingle {
            val g = pool.start {
                crossThreadGenerator<Int> {
                    yield(10)
                    throw RuntimeException(errText)
                }
            }
            assertFalse(g.isFinished)
            assertEquals(10, g.next())
            assertFalse(g.isFinished)

            try {
                g.next()
                fail("Generator should throw RuntimeException with text \"$errText\"")
            } catch (e: RuntimeException) {
                assertEquals(errText, e.message)
            }
            assertTrue(g.isFinished)
            try {
                g.next()
                fail()
            } catch (e: NoSuchElementException) {
                //Do nothing
            }
        }
        pool.shutdown()
        nd.close()
    }
}