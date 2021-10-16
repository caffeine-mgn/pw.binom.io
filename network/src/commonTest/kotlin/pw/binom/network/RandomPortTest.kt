package pw.binom.network

import kotlin.test.Test

class RandomPortTest {
    @Test
    fun tcpRandomPortTest(){
        TcpServerConnection.randomPort()
    }
}