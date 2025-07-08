package pw.binom.radis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.db.radis.RadisConnection
import pw.binom.db.radis.RadisConnectionImpl
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.useAsync
import pw.binom.network.Network

abstract class BaseRedisTest {
  fun redisTest(func: suspend (RadisConnectionImpl) -> Unit) = runTest {
    val address = InetSocketAddress.resolve(host = "127.0.0.1", port = 7133)
    RadisConnection.connect(address).useAsync { con ->
      withContext(Dispatchers.Network) {
        func(con)
      }
    }
  }
}
