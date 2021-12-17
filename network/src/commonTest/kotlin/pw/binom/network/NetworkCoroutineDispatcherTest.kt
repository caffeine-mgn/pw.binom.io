package pw.binom.network

import kotlinx.coroutines.test.runTest
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.use
import kotlin.test.Test

class NetworkCoroutineDispatcherTest {
    @Test
    fun clientTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        println("Dispatcher: ${getDispatcher()}")
        val client = nd.tcpConnect(NetworkAddress.Immutable("example.com", 80))
//            val client = nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1",4444))
        println("Connected!")
        client.bufferedAsciiWriter(closeParent = false).use {
            it.append("GET / HTTP/1.1\r\n")
                .append("Host: example.com\r\n")
                .append("\r\n")
            it.flush()
        }
        println("Getting data...")
        val text = client.bufferedAsciiReader(closeParent = false).use {
            it.readln()
        }
        println("txt: $text")
        println("Dispatcher: ${getDispatcher()}")
    }
}