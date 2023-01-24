package pw.binom.socket

import pw.binom.io.socket.Epoll
import kotlin.test.Test

class EpollTest {
    @Test
    fun addTest() {
        val epoll = Epoll.create(500)
        epoll.close()
    }
}
