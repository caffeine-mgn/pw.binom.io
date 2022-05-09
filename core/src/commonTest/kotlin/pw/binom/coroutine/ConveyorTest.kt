package pw.binom.coroutine

// import pw.binom.async2
// import pw.binom.getOrException
// import pw.binom.io.ClosedException
// import pw.binom.nextUuid
// import kotlin.random.Random
// import kotlin.test.*
//
// class ConveyorTest {
//    @Test
//    fun test() {
//        async2 {
//            val conveyor = conveyor<Int> {
//                assertEquals(10, consume())
//                assertEquals(20, consume())
//                assertEquals(30, consume())
//            }
//            assertFalse(conveyor.isFinished)
//            conveyor.submit(10)
//            assertFalse(conveyor.isFinished)
//            conveyor.submit(20)
//            assertFalse(conveyor.isFinished)
//            conveyor.submit(30)
//            assertTrue(conveyor.isFinished)
//            assertNull(conveyor.exceptionOrNull)
//
//        }.getOrException()
//    }
//
//    @Test
//    fun conveyorEndTest() {
//        async2 {
//            val conveyor = conveyor<Int> {
//                assertEquals(10, consume())
//            }
//            assertFalse(conveyor.isFinished)
//            conveyor.submit(10)
//            assertTrue(conveyor.isFinished)
//            assertNull(conveyor.exceptionOrNull)
//            try {
//                conveyor.submit(20)
//                fail()
//            } catch (e: ClosedException) {
//                //Do nothing
//            }
//        }.getOrException()
//    }
//
//    @Test
//    fun exceptionInsideConveyor() {
//        val errTxt = Random.nextUuid().toString()
//        async2 {
//            val conveyor = conveyor<Int> {
//                assertEquals(10, consume())
//                throw RuntimeException(errTxt)
//            }
//
//            conveyor.submit(10)
//            assertTrue(conveyor.isFinished)
//            assertNull(conveyor.exceptionOrNull)
//            assertTrue(conveyor.exceptionOrNull is RuntimeException)
//            assertEquals(errTxt, conveyor.exceptionOrNull!!.message)
//        }
//    }
//
//    @Test
//    fun closeConveyor() {
//        async2 {
//            val conveyor = conveyor<Int> {
//                try {
//                    consume()
//                    fail()
//                } catch (e: ClosedException) {
//                    //Do nothing
//                }
//            }
//            conveyor.asyncClose()
//            assertTrue(conveyor.isFinished)
//            assertNull(conveyor.exceptionOrNull)
//        }
//    }
// }
