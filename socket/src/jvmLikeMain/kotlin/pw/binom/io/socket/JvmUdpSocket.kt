package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

class JvmUdpSocket(override val native: DatagramChannel) : UdpSocket, UdpUnixSocket, UdpNetSocket {
    override fun close() {
        native.close()
    }

    override var blocking: Boolean = false
        set(value) {
            field = value
            native.configureBlocking(value)
        }

    private var internalPort = 0

    override val port: Int?
        get() = internalPort.takeIf { it != 0 }

    override fun bind(address: NetworkAddress): BindStatus {
        try {
            native.bind(address.toJvmAddress().native)
            internalPort = native.socket().localPort
            return BindStatus.OK
        } catch (e: java.net.BindException) {
            return BindStatus.ADDRESS_ALREADY_IN_USE
        }
    }

    override fun send(data: ByteBuffer, address: NetworkAddress): Int {
        return native.send(data.native, address.toJvmAddress().native)
    }

    override fun receive(data: ByteBuffer, address: MutableNetworkAddress?): Int {
        val before = data.position
        if (before == data.remaining) {
            return 0
        }
        val remoteAddress = native.receive(data.native)
        if (remoteAddress != null && address != null) {
            remoteAddress as InetSocketAddress
            if (address is JvmMutableNetworkAddress) {
                address.native = remoteAddress
            } else {
                address.update(
                    host = remoteAddress.address.hostAddress,
                    port = remoteAddress.port,
                )
            }
        }
        return data.position - before
    }

    override fun bind(path: String): BindStatus {
        native.socket().reuseAddress = true
        native.bindUnix(path)
        return BindStatus.OK
    }

    override fun send(data: ByteBuffer, address: String): Int = native.sendUnix(address, data)

    override fun receive(data: ByteBuffer, address: (String) -> Unit?): Int {
        val before = data.position
        native.receive(data.native)
        return data.position - before
    }

    override val tcpNoDelay: Boolean
        get() = false

    override fun setTcpNoDelay(value: Boolean): Boolean = false
}
