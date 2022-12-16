package pw.binom.io.socket

import java.net.InetSocketAddress
import java.net.UnixDomainSocketAddress
import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel
import java.nio.file.Files
import kotlin.io.path.Path

class JvmTcpServerSocket(override val native: ServerSocketChannel) : TcpNetServerSocket, TcpUnixServerSocket {
    override fun close() {
        native.close()
    }

    override fun accept(address: MutableNetworkAddress?): TcpClientNetSocket? {
        val client = native.accept() ?: return null
        if (address != null) {
            val addr = client.socket().localAddress as InetSocketAddress
            if (address is JvmMutableNetworkAddress) {
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

    override fun bind(address: NetworkAddress): BindStatus {
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
        val address = Path(path)
        Files.deleteIfExists(address)
        try {
            native.bind(UnixDomainSocketAddress.of(address))
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
}
