package pw.binom.io.socket

import pw.binom.doFreeze
import pw.binom.io.InputStream
import pw.binom.io.OutputStream


actual class SocketChannel internal constructor(override val socket: Socket) : NetworkChannel, InputStream, OutputStream {
    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }

    init {
        doFreeze()
    }

    actual constructor() : this(Socket())

    actual val isConnected: Boolean
        get() = socket.connected

    override fun flush() {
        //NOP
    }

    override fun close() {
        socket.close()
    }

    actual fun connect(host: String, port: Int) {
        socket.connect(host, port)
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int = socket.read(data = data, offset = offset, length = length)

    override fun write(data: ByteArray, offset: Int, length: Int) =
            try {
                socket.write(data = data, offset = offset, length = length)
            } catch (e: Throwable) {
                throw e
            }
}