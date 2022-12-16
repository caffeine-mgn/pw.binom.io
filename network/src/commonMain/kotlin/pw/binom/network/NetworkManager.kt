package pw.binom.network

import pw.binom.io.socket.*
import kotlin.coroutines.CoroutineContext

interface NetworkManager : CoroutineContext {
    fun wakeup()
    fun attach(channel: UdpNetSocket): UdpConnection
    fun attach(channel: TcpClientSocket, mode: Int = 0): TcpConnection
    fun attach(channel: TcpNetServerSocket): TcpServerConnection

    fun bindTcp(address: NetworkAddress): TcpServerConnection {
        val channel = Socket.createTcpServerNetSocket()
        channel.blocking = false
        val connection = attach(channel)
        when (channel.bind(address)) {
            BindStatus.ADDRESS_ALREADY_IN_USE -> {
                connection.close()
                throw BindException()
            }

            BindStatus.ALREADY_BINDED -> {
                connection.close()
                throw IllegalStateException()
            }

            BindStatus.OK -> {
                val actualBindAddress = NetworkAddress.create(
                    host = address.host,
                    port = connection.port
                )
                connection.description = "Server $actualBindAddress"
                return connection
            }
        }
    }
}

suspend fun NetworkManager.tcpConnect(address: NetworkAddress): TcpConnection {
    val channel = Socket.createTcpClientNetSocket()
    val connectStatus = channel.connect(address)
    if (connectStatus != ConnectStatus.IN_PROGRESS && connectStatus != ConnectStatus.OK) {
        channel.close()
        throw SocketConnectException("Invalid connect status: $connectStatus")
    }
    channel.blocking = false
    val connection = attach(channel = channel)
//    try {
    connection.description = "Connect to $address"
    connection.connection()
//    } catch (e: SocketConnectException) {
//        runCatching { connection.asyncClose() }
//        if (e.message != null) {
//            throw e
//        } else {
//            throw SocketConnectException(address.toString(), e.cause)
//        }
//    }
    return connection
}

suspend fun NetworkManager.tcpConnectUnixSocket(fileName: String): TcpConnection {
    val channel = Socket.createTcpClientUnixSocket()
    channel.connect(fileName)
    val connection = attach(channel = channel)
    try {
        connection.description = "Unix socket \"$fileName\""
        connection.connection()
    } catch (e: SocketConnectException) {
        runCatching { connection.asyncClose() }
        if (e.message != null) {
            throw e
        } else {
            throw SocketConnectException(fileName, e.cause)
        }
    }
    return connection
}
//    TODO добавить поддержку tcp/unix
//    fun NetworkManager.bindTcpUnixSocket(fileName: String): TcpServerConnection {
//        val channel = Socket.createTcpServerUnixSocket()
//        channel.blocking = false
//        channel.bind(fileName)
//        val connection = attach(channel)
//        connection.description = fileName
//        return connection
//    }

fun NetworkManager.bindUdp(address: NetworkAddress): UdpConnection {
    val channel = Socket.createUdpNetSocket()
    channel.blocking = false
    channel.bind(address)
    val connection = attach(channel)
    connection.description = address.toString()
    return connection
}
