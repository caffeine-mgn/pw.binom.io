package pw.binom.network

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException

interface NetworkManager : CoroutineContext {
    fun wakeup()
    fun attach(channel: UdpSocketChannel): UdpConnection
    fun attach(channel: TcpClientSocketChannel, mode: Int = 0): TcpConnection
    fun attach(channel: TcpServerSocketChannel): TcpServerConnection
}

suspend fun NetworkManager.tcpConnect(address: NetworkAddress): TcpConnection {
    val channel = TcpClientSocketChannel()
    val connection = attach(channel = channel, mode = Selector.EVENT_CONNECTED or Selector.EVENT_ERROR)
    try {
        connection.description = address.toString()
        suspendCancellableCoroutine<Unit> {
            connection.connect = it
            it.invokeOnCancellation {
                connection.cancelSelector()
                connection.close()
            }
            try {
//                        connection.connecting()
                channel.connect(address)
                wakeup()
            } catch (e: Throwable) {
                it.resumeWithException(e)
            }
        }
    } catch (e: SocketConnectException) {
        runCatching { connection.asyncClose() }
        if (e.message != null) {
            throw e
        } else {
            throw SocketConnectException(address.toString(), e.cause)
        }
    }
    return connection
}

suspend fun NetworkManager.tcpConnectUnixSocket(fileName: String): TcpConnection {
    val channel = TcpClientSocketChannel()
    val connection = attach(channel = channel, mode = Selector.EVENT_CONNECTED or Selector.EVENT_ERROR)
    try {
        connection.description = fileName
        suspendCancellableCoroutine<Unit> {
            connection.connect = it
            it.invokeOnCancellation {
                connection.cancelSelector()
                connection.close()
            }
            try {
                channel.connect(fileName)
                wakeup()
            } catch (e: Throwable) {
                it.resumeWithException(e)
            }
        }
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

fun NetworkManager.bindTcp(address: NetworkAddress): TcpServerConnection {
    val channel = TcpServerSocketChannel()
    channel.setBlocking(false)
    channel.bind(address)
    return attach(channel)
}

fun NetworkManager.bindTcpUnixSocket(fileName: String): TcpServerConnection {
    val channel = TcpServerSocketChannel()
    channel.setBlocking(false)
    channel.bind(fileName)
    return attach(channel)
}

fun NetworkManager.bindUdp(address: NetworkAddress): UdpConnection {
    val channel = UdpSocketChannel()
    channel.setBlocking(false)
    channel.bind(address)
    return attach(channel)
}
