package pw.binom.io.socket.nio

import kotlin.test.Test
import kotlin.test.assertEquals

/*
class WaitEventQueueTest {

    @Test
    fun test() {

        val data = (0 until 100).map { SocketNIOManager.WaitEvent() }

        val q = WaitEventQueue<SocketNIOManager.WaitEvent>(0f,0f)

        data.forEach {
            q.push(it)
        }

        assertEquals(100, q.size)
        val e1 = SocketNIOManager.WaitEvent()
        val e2 = SocketNIOManager.WaitEvent()
        q.push(e1)
        assertEquals(e1, q.pop())

        q.push(e1)
        q.push(e2)
        assertEquals(102, q.size)
        assertEquals(e2, q.pop())
        assertEquals(e1, q.pop())

        (99 downTo 0).forEach {
            assertEquals(data[it], q.pop())
        }
        assertEquals(0, q.size)
        println()
    }
}*/
