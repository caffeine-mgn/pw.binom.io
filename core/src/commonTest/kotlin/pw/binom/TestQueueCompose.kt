package pw.binom

import pw.binom.thread.FreezedStack
import pw.binom.thread.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestQueueCompose {

    @Test
    fun testFirstEmpty() {
        val queue1 = FreezedStack<String>().asFiFoQueue()
        val queue2 = FreezedStack<String>().asFiFoQueue()

        val queue = queue1 + queue2


        queue2.push("1")
        queue2.push("2")

        assertEquals("1", queue.pop())
        assertEquals("2", queue.pop())
        try {
            queue.pop()
            fail()
        } catch (e: NoSuchElementException) {
            //NOP
        }
    }

    @Test
    fun testSecondEmpty() {
        val queue1 = FreezedStack<String>().asFiFoQueue()
        val queue2 = FreezedStack<String>().asFiFoQueue()

        val queue = queue1 + queue2


        queue1.push("1")
        queue1.push("2")

        assertEquals("1", queue.pop())
        assertEquals("2", queue.pop())
        try {
            queue.pop()
            fail()
        } catch (e: NoSuchElementException) {
            //NOP
        }
    }

    @Test
    fun testNotFull(){
        val queue1 = FreezedStack<String>().asFiFoQueue()
        val queue2 = FreezedStack<String>().asFiFoQueue()

        val queue = queue1 + queue2


        queue2.push("1")
        queue1.push("2")
        queue2.push("3")
        queue1.push("4")
        queue2.push("5")
        queue2.push("6")

        assertEquals("1", queue.pop())
        assertEquals("2", queue.pop())
        assertEquals("3", queue.pop())
        assertEquals("4", queue.pop())
        assertEquals("5", queue.pop())
        assertEquals("6", queue.pop())
        try {
            queue.pop()
            fail()
        } catch (e: NoSuchElementException) {
            //NOP
        }
    }
}