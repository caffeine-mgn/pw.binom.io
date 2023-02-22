package pw.binom.io.db.firebird.async

import pw.binom.db.TransactionMode
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.db.async.DatabaseInfo
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.writeByteArray
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.readByte

val protocols = arrayOf(
    // PROTOCOL_VERSION, Arch type (Generic=1), min, max, weight
    "0000000a00000001000000000000000500000002", // 10, 1, 0, 5, 2
    "ffff800b00000001000000000000000500000004", // 11, 1, 0, 5, 4
    "ffff800c00000001000000000000000500000006", // 12, 1, 0, 5, 6
    "ffff800d00000001000000000000000500000008", // 13, 1, 0, 5, 8
    "ffff800e0000000100000000000000050000000a", // 14, 1, 0, 5, 10
    "ffff800f0000000100000000000000050000000c", // 15, 1, 0, 5, 12
    "ffff80100000000100000000000000050000000e", // 16, 1, 0, 5, 14
    "ffff801100000001000000000000000500000010", // 17, 1, 0, 5, 16
)

class FirebirdConnection : AsyncConnection {
    companion object {
        suspend fun connect(
            address: NetworkAddress,
            databaseName: String,
            login: String,
            password: String,
            networkManager: NetworkManager,
            wire_crypt: Boolean,
            auth_plugin_name: String,
            clientPublic: ByteArray?,
        ) {
            val buffer = ByteBuffer(8)
            val connection = networkManager.tcpConnect(address)
            val wire = WireProtocol(connection)
            wire.packInt(op_connect)
            wire.packInt(op_attach)
            wire.packInt(3) // CONNECT_VERSION3
            wire.packInt(1) // Arch type(GENERIC)
            wire.packString(databaseName)
            wire.packInt(protocols.size)
            wire.writeXDRBytes(
                wire.uid(
                    login,
                    password,
                    auth_plugin_name,
                    wire_crypt,
                    clientPublic,
                ),
            )
            connection.writeByteArray(
                protocols.joinToString("").windowed(2).map { it.toUByte(16).toByte() }.toTypedArray().toByteArray(),
                buffer,
            )
            println("Reading response...")
            while (true) {
                val byte = connection.readByte(buffer)
                println("-->${byte.toUByte().toString(16)} (${byte.toUByte()})")
            }
        }
    }

    override val type: String
        get() = "firebird"
    override val isConnected: Boolean
        get() = TODO("Not yet implemented")
    override val dbInfo: DatabaseInfo
        get() = TODO("Not yet implemented")

    override suspend fun setTransactionMode(mode: TransactionMode) {
        TODO("Not yet implemented")
    }

    override val transactionMode: TransactionMode
        get() = TODO("Not yet implemented")

    override suspend fun createStatement(): AsyncStatement {
        TODO("Not yet implemented")
    }

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement {
        TODO("Not yet implemented")
    }

    override fun isReadyForQuery(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun beginTransaction() {
        TODO("Not yet implemented")
    }

    override suspend fun commit() {
        TODO("Not yet implemented")
    }

    override suspend fun rollback() {
        TODO("Not yet implemented")
    }

    override suspend fun asyncClose() {
        TODO("Not yet implemented")
    }
}
