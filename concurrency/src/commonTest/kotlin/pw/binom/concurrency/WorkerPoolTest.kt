package pw.binom.concurrency

// @OptIn(ExperimentalTime::class)
// class WorkerPoolTest {
//
//    @Test
//    fun shutdownTestEmpty() {
//        val w = WorkerPool()
//        w.submit {
//            sleep(1000)
//        }.joinAndGetOrThrow()
//        val shutdownTime = measureTime {
//            w.shutdown()
//        }
//        assertTrue(shutdownTime < 100.milliseconds)
//    }
//
//    @Test
//    fun shutdownTestNotEmpty() {
//        val w = WorkerPool()
//        val lock = SpinLock()
//        lock.lock()
//        w.submit {
//            lock.synchronize {
//                val r = TimeSource.Monotonic.markNow()
//                sleep(1000)
//                println("Sleep time ${r.elapsedNow()}")
//            }
//        }
//        val shutdownTime = measureTime {
//            lock.unlock()
//            w.shutdown()
//        }
//        val msg = "shutdownTime=$shutdownTime"
//        assertTrue(shutdownTime > 1.seconds, msg)
//        assertTrue(shutdownTime < 1.5.seconds, msg)
//    }
// }
