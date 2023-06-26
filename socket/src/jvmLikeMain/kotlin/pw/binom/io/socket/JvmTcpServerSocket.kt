package pw.binom.io.socket

import java.net.InetSocketAddress
import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel

class JvmTcpServerSocket(override val native: ServerSocketChannel) : TcpNetServerSocket, TcpUnixServerSocket {
    override fun close() {
        native.close()
    }

    override fun accept(address: MutableInetNetworkAddress?): TcpClientNetSocket? {
        val client = native.accept() ?: return null
        if (address != null) {
            val addr = client.socket().localAddress as InetSocketAddress
            if (address is JvmMutableInetNetworkAddress) {
                address.native = addr
            } else {
                address.update(
                    host = addr.address.hostAddress,
                    port = addr.port,
                )
            }
        }
        return JvmTcpClientSocket(client)
    }

    override fun bind(address: InetNetworkAddress): BindStatus {
        try {
            native.socket().reuseAddress = true
            native.bind(address.toJvmAddress().native)
        } catch (e: AlreadyBoundException) {
            return BindStatus.ALREADY_BINDED
        } catch (e: java.net.BindException) {
            return BindStatus.ADDRESS_ALREADY_IN_USE
        }
        return BindStatus.OK
    }

    override fun accept(address: ((String) -> Unit)?): TcpClientNetSocket? {
        val newClient = native.accept() ?: return null
        return JvmTcpClientSocket(newClient)
    }

    override fun bind(path: String): BindStatus {
        try {
            native.bindUnix(path)
        } catch (e: AlreadyBoundException) {
            return BindStatus.ALREADY_BINDED
        }
        return BindStatus.OK
    }

    override var blocking: Boolean = false
        set(value) {
            field = value
            native.configureBlocking(value)
        }
    override val port: Int?
        get() = native.socket().localPort.takeIf { it != -1 }
    override val tcpNoDelay: Boolean
        get() = false

    override fun setTcpNoDelay(value: Boolean): Boolean = false
}
