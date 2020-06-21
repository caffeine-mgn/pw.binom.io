package pw.binom.io.socket.ssl

import pw.binom.doFreeze
import pw.binom.io.socket.NetworkChannel
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.Socket
import pw.binom.io.socket.SocketChannel

/*
actual class SSLSocketChannel(val sslSocket: SSLSocket) : SocketChannel, NetworkChannel {
    override val socket: Socket
        get() = sslSocket

    override val nsocket: RawSocket
        get() = sslSocket.raw
    override val type: Int
        get() = 0x001b

    override var blocking: Boolean
        get() = nsocket.blocking
        set(value) {
            nsocket.blocking = value
        }

    init {
        doFreeze()
    }

    override val isConnected: Boolean
        get() = socket.connected

    override fun flush() {
        //NOP
    }

    override fun close() {
        socket.close()
    }

    override val available: Int
        get() = socket.input.available

    override fun connect(host: String, port: Int) {
        socket.connect(host, port)
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int =
            socket.input.read(data = data, offset = offset, length = length)

    override fun write(data: ByteArray, offset: Int, length: Int) =
            socket.output.write(data = data, offset = offset, length = length)
}*/
