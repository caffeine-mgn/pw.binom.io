package pw.binom.concurrency

//import pw.binom.coroutine.crossThreadConveyor
//import pw.binom.coroutine.crossThreadGenerator
//import pw.binom.coroutine.start
//import pw.binom.network.NetworkDispatcher
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//class CrossThreadConveyorTest {
//    @Test
//    fun test() {
//        val nd = NetworkDispatcher()
//        val pool = WorkerPool()
//        nd.runSingle {
//            val g = pool.start {
//                crossThreadConveyor<Int> {
//                    assertEquals(10,consume())
//                    assertEquals(20,consume())
//                }
//            }
//            assertFalse(g.isFinished)
//            g.submit(10)
//            assertFalse(g.isFinished)
//            g.submit(20)
//            assertTrue(g.isFinished)
//        }
//        pool.shutdown()
//        nd.close()
//    }
//}