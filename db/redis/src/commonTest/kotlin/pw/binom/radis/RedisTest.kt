package pw.binom.radis

import kotlinx.coroutines.test.runTest
import pw.binom.db.radis.RadisConnection
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test

class RedisTest : BaseRedisTest() {
    @Test
    fun ff() = runTest {
        val address = NetworkAddress.Immutable(host = "127.0.0.1", port = 6379)
        RadisConnection.connect(address).use { con ->
            con.ping()
//            println("->${con.info()}")
            con.setString("test", "value")
            con.setString("test1", "Hello Антон")
            con.insertFirst("my_list", Random.nextUuid().toString())
            println("------------>GET 'test'")
            println("1-->value: \"${con.getString("test")}\"")
            println("2-->value: \"${con.getList("my_list")}\"")
        }
    }
}
