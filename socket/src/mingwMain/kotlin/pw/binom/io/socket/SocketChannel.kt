package pw.binom.io.socket

import platform.posix.SOCKET
import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import kotlin.native.concurrent.ensureNeverFrozen


actual class SocketChannel internal constructor(internal val socket: Socket) : Channel,InputStream,OutputStream {

    init {
        this.ensureNeverFrozen()
    }

    override val native: SOCKET
        get() = socket.native

    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }

    actual constructor() : this(Socket())

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

    override fun write(data: ByteArray, offset: Int, length: Int) = socket.write(data = data, offset = offset, length = length)
}