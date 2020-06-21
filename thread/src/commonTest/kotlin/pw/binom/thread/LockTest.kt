package pw.binom.thread

import pw.binom.Console
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.closablesOf
import pw.binom.io.hold
import pw.binom.printStacktrace
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.*

private const val LOCKED = 1
private const val UNLOCKED = 0


class Water(locked: Boolean) {
    val state = AtomicBoolean(locked)

    fun lock() {
        while (true) {
            if (state.compareAndSet(false, true))
                break
        }
    }

    fun unlock() {
        while (true) {
            if (state.compareAndSet(true, false))
                break
        }
    }
}

@Ignore
@OptIn(ExperimentalTime::class)
class LockTest {

    @Test
    fun testLock() {
        val lock = Lock()
        var duration = Duration.ZERO
        val water = Water(true)
        closablesOf(lock).hold {
            val thread = Thread(Runnable {
                water.unlock()
                val vv = measureTime {
                    lock.synchronize {
                        println("Hello from other thread")
                    }
                }
                duration = vv
            })

            lock.synchronize {
                thread.start()
                water.lock()
                Thread.sleep(1000)
            }

            thread.join()
            assertTrue(duration >= 900.0.toDuration(DurationUnit.MILLISECONDS) && duration <= 1500.0.toDuration(DurationUnit.MILLISECONDS))
        }
    }

    @Test
    fun testCondition() {
        val water = Water(true)
        val waterLock = Water(true)
        val lock = Lock()
        val condition = lock.newCondition()
        var duration = Duration.ZERO
        closablesOf(condition, lock).hold {
            val thread = Thread(Runnable {
                water.unlock()
                val vv = measureTime {
                    lock.synchronize {
                        try {
                            condition.wait()
                        } catch (e: Throwable) {
                            e.printStacktrace(Console.std)
                        }

                    }
                }
                duration = vv
            })

            thread.start()
            water.lock()
            Thread.sleep(1000)


            lock.synchronize {
                condition.notifyAll()
            }
            thread.join()
            assertTrue(duration >= 900.0.toDuration(DurationUnit.MILLISECONDS) && duration <= 1500.0.toDuration(DurationUnit.MILLISECONDS))
        }
    }
}