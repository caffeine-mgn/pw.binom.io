package pw.binom.db.radis

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcher

interface RadisConnection : AsyncCloseable {
    enum class ValueType {
        STRING, LIST, SET, ZSET, HASH, STREAM
    }

    val readyForRequest: Boolean

    companion object {
        suspend fun connect(
            address: NetworkAddress,
            manager: NetworkCoroutineDispatcher = Dispatchers.Network,
            login: String? = null,
            password: String? = null,
        ): RadisConnectionImpl {
            val con = RadisConnectionImpl(manager.tcpConnect(address))
            try {
                con.start()
                return con
            } catch (e: Throwable) {
                con.asyncClose()
                throw e
            }
        }
    }
}
