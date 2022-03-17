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
        RadisConnection.connect(address).use { con ->
            con.ping()
            con.set("test", "value")
            con.set("test1", "value1")
            println("-->${con.get("test")}")
        }
    }
}
