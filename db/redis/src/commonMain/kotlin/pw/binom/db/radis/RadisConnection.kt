package pw.binom.db.radis

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcher

interface RadisConnection : AsyncCloseable {
    object NumberNull
    companion object {
        suspend fun connect(
            address: NetworkAddress,
            manager: NetworkCoroutineDispatcher = Dispatchers.Network,
            login: String?,
            password: String?,
        ) = RadisConnectionImpl(manager.tcpConnect(address))
    }
}
