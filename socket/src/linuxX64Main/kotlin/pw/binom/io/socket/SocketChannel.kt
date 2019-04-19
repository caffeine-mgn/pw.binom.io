package pw.binom.io.socket

import pw.binom.io.InputStream
import pw.binom.io.OutputStream

actual class SocketChannel(internal val socket: Socket) : Channel,InputStream,OutputStream {
    override fun flush() {
    }

    override fun close() {
        socket.close()
    }

    actual constructor() : this(Socket())

    actual fun connect(host: String, port: Int) {
        socket.connect(host, port)
    }

    override actual fun read(data: ByteArray, offset: Int, length: Int): Int = socket.read(data = data, offset = offset, length = length)

    override actual fun write(data: ByteArray, offset: Int, length: Int): Int = socket.write(data = data, offset = offset, length = length)

    actual fun write(data: String): Int = write(data.toUtf8OrThrow())
    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
}