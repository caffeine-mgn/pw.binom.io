package pw.binom.io.db.firebird.async

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.Network
import kotlin.test.Test

class FirebirdConnectionTest {
    @Test
    fun test() = runTest {
        println("Try connect...")
        val connection = FirebirdConnection.connect(
            address = NetworkAddress.create(
                host = "127.0.0.1",
                port = 3050,
            ),
            databaseName = "/firebird/data/test",
            login = "sysdba",
            password = "sysdba",
            networkManager = Dispatchers.Network,
            wire_crypt = false,
            auth_plugin_name = "Legacy_Auth",
            clientPublic = null,
        )
        println("Connected!")
    }
}
