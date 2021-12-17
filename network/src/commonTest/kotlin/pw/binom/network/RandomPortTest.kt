package pw.binom.network

import kotlin.test.Test
import kotlin.test.assertTrue

class RandomPortTest {
    @Test
    fun tcpRandomPortTest() {
        assertTrue(TcpServerConnection.randomPort() > 0)
    }
}