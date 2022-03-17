package pw.binom.radis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pw.binom.db.radis.RadisConnection
import pw.binom.io.use
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import kotlin.test.Test

class RedisTest : BaseRedisTest() {
    @Test
    fun ff() = runTest {
        val address = NetworkAddress.Immutable(host = "127.0.0.1", port = 6379)
        val c = Dispatchers.Network.tcpConnect(address)
        RadisConnection.connect(address).use { con ->
            con.ping()
        }

//        c.bufferedWriter(closeParent = false).use {
//            it.append("PING\r\n")
//            it.flush()
//        }
//        c.bufferedReader(closeParent = false).use { r ->
//            println("Read text...")
//            val txt = r.readln()
//            println("txt: $txt")
//        }
    }
}
