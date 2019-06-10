package pw.binom.io.socket

import pw.binom.doFreeze


actual class RawSocketChannel internal constructor(override val socket: RawSocket) : SocketChannel, NetworkChannel {
    override val nsocket: RawSocket
        get() = socket
    override var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }

    init {
        doFreeze()
    }

    actual constructor() : this(RawSocket())

    override val isConnected: Boolean
        get() = socket.connected

    override fun flush() {
        //NOP
    }

    override fun close() {
        socket.close()
    }

    override fun connect(host: String, port: Int) {
        socket.connect(host, port)
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int =
            socket.input.read(data = data, offset = offset, length = length)

    override fun write(data: ByteArray, offset: Int, length: Int) =
            socket.output.write(data = data, offset = offset, length = length)

    override val available: Int
        get() = socket.input.available
}