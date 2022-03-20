package pw.binom.coroutine

import kotlin.test.*

// class AbstractGeneratorTest {
//
//    @Test
//    fun simpleTest() {
//        runBlocking {
//            val g = generator<Int> {
//                yield(10)
//                yield(20)
//                30
//            }
//            assertFalse(g.isFinished)
//            assertEquals(10, g.next())
//            assertFalse(g.isFinished)
//            assertEquals(20, g.next())
//            assertFalse(g.isFinished)
//            assertEquals(30, g.next())
//            assertTrue(g.isFinished)
//            try {
//                g.next()
//                fail()
//            } catch (e: NoSuchElementException) {
//                //Do nothing
//            }
//        }.getOrException()
//    }
//
//    @Test
//    fun exceptionTest() {
//        val errText = Random.nextUuid().toShortString()
//        async2 {
//            val g = generator<Int> {
//                yield(10)
//                throw RuntimeException(errText)
//            }
//            assertFalse(g.isFinished)
//            assertEquals(10, g.next())
//            assertFalse(g.isFinished)
//
//            try {
//                g.next()
//                fail("Generator should throw RuntimeException with text \"$errText\"")
//            } catch (e: RuntimeException) {
//                assertEquals(errText, e.message)
//            }
//            assertTrue(g.isFinished)
//            try {
//                g.next()
//                fail()
//            } catch (e: NoSuchElementException) {
//                //Do nothing
//            }
//        }.getOrException()
//    }
// }
