package pw.binom.network

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.Environment
import pw.binom.OS
import pw.binom.io.use
import pw.binom.nextUuid
import pw.binom.os
import kotlin.random.Random
import kotlin.test.Test

class UnixSocketTest {
    @Test
    fun baseTest() = runTest {
        if (Environment.os == OS.WINDOWS) {
            return@runTest
        }
        val address = "/tmp/${Random.nextUuid()}"
        NetworkCoroutineDispatcherImpl().use { nd ->
            withContext(nd) {
                val server = nd.bindTcpUnixSocket(address)
                GlobalScope.launch(nd) {
                    println("wait connection")
                    val newClient = server.accept()
                    println("New client connected!")
                }
                println("try connect")
                val client = nd.tcpConnectUnixSocket(address)
                println("Connected!")
            }
        }
    }
}
