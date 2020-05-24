package pw.binom

import pw.binom.thread.FreezedStack
import kotlin.test.*

class FreezedStackTest {

    /*
    @Test
    @Ignore
    fun threding() {
        class SomeTask(val input: Queue<Int>, val output: AppendableQueue<Int>) : Task() {
            override fun execute() {
                while (!isInterrupted) {
                    input.popAwait(5_000)
                    val value = input.popOrNull()
                    if (value == null) {
                        Thread.sleep(10)
                        continue
                    }
                    output.push(value * value)
                }
            }
        }

        val output = FreezedStack<Int>().asFiFoQueue()
        val input = FreezedStack<Int>().asFiFoQueue()

        val w = Worker.execute { SomeTask(input, output) }

        fun test(value: Int) {
            input.push(value)
            assertEquals(value * value, output.popAwait(5_000))
        }

        test(10)
        test(20)
        test(30)
        test(40)
        test(50)

        w.interrupt()
    }
    */

    @Test
    fun iteratorRemove1(){
        val f = FreezedStack<String>()
        f.pushLast("1")
        f.pushLast("2")
        f.pushLast("3")
        f.pushLast("4")

        val it = f.iterator()
        assertEquals("1",it.next())
        assertEquals("2",it.next())
        it.remove()
        assertEquals("3",it.next())
        assertEquals("4",it.next())

        assertTrue(f.any { it=="1" })
        assertFalse(f.any { it=="2" })
        assertTrue(f.any { it=="3" })
        assertTrue(f.any { it=="4" })
    }

    @Test
    fun iteratorRemove2(){
        val f = FreezedStack<String>()
        f.pushLast("1")
        f.pushLast("2")
        f.pushLast("3")
        f.pushLast("4")

        f.removeAll { it=="2" }

        assertTrue(f.any { it=="1" })
        assertFalse(f.any { it=="2" })
        assertTrue(f.any { it=="3" })
        assertTrue(f.any { it=="4" })
    }
    @Test
    fun iteratorRemove3(){
        val f = FreezedStack<String>()
        f.pushLast("2")
        f.removeAll { it=="2" }
        assertFalse(f.any { it=="2" })
        assertEquals(0, f.size)
    }

    @Test
    fun iteratorRemove4(){
        val f = FreezedStack<String>()
        f.removeAll { it=="2" }
        assertFalse(f.any { it=="2" })
        assertEquals(0, f.size)
    }
}