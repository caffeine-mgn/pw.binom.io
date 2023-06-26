package pw.binom.io.socket

import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import platform.common.internal_close_socket
import platform.common.internal_getSocketPort
import platform.common.internal_send_to_socket_udp
import platform.common.internal_tcp_nodelay
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import kotlin.native.identityHashCode

abstract class AbstractSocket(override val native: RawSocket, override val server: Boolean) :
    TcpClientUnixSocket,
    TcpClientNetSocket,
    TcpUnixServerSocket,
    TcpNetServerSocket,
    UdpUnixSocket,
    UdpNetSocket {

    protected var closed = false

    override fun toString(): String =
        "Socket(id:${this.identityHashCode().toUInt().toString(16)}, fd: $native)"

    private var internalTcpNoDelay = false
    override val tcpNoDelay: Boolean
        get() = internalTcpNoDelay

    override var blocking: Boolean = false
        set(value) {
            field = value
            setBlocking(native, value)
        }

    override val port: Int?
        get() = internal_getSocketPort(native).takeIf { it != -1 }

    protected fun nativeClose() {
        internal_close_socket(native)
    }

    override fun receive(data: ByteBuffer, address: MutableInetNetworkAddress?): Int {
        val received = internalReceive(
            native = native,
            data = data,
            address = address,
        )
        if (received > 0) {
            data.position += received
        }
        return received
    }

    protected fun ensureOpen() {
        if (!closed) {
            throw ClosedException()
        }
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        nativeClose()
    }

    protected abstract fun processAfterSendUdp(data: ByteBuffer, code: Int): Int

    override fun send(data: ByteBuffer, address: InetNetworkAddress): Int {
        if (data.remaining == 0) {
            return 0
        }
        val netAddress = if (address is CommonMutableInetNetworkAddress) {
            address
        } else {
            CommonMutableInetNetworkAddress(address)
        }
        val sendResult = netAddress.getAsIpV6 { ipv6Addr ->
            data.ref(0) { ptr, remaining ->
                internal_send_to_socket_udp(
                    native,
                    ptr,
                    remaining.convert(),
                    0,
                    ipv6Addr.reinterpret(),
                )
            }
        }.toInt()
        return processAfterSendUdp(data, sendResult)
    }

    override fun accept(address: MutableInetNetworkAddress?): TcpClientNetSocket? {
        val clientRaw = internalAccept(native, address) ?: return null
        return createSocket(socket = clientRaw, server = false) as TcpClientNetSocket
    }

    override fun setTcpNoDelay(value: Boolean): Boolean {
        val result = internal_tcp_nodelay(native, if (value) 1 else 0) > 0
        if (result) {
            internalTcpNoDelay = value
        }
        return result
    }
}
