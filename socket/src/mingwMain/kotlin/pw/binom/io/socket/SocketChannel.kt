package pw.binom.io.socket

import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import kotlin.native.concurrent.ensureNeverFrozen


actual class SocketChannel internal constructor(override val socket: Socket) : NetworkChannel, InputStream, OutputStream {
    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }

    init {
        this.ensureNeverFrozen()
    }

    private val keys = HashMap<SocketSelector, SocketSelector.SelectorKey>()

    override fun regSelector(selector: SocketSelector, key: SocketSelector.SelectorKey) {
        if (keys.containsKey(selector))
            throw IllegalArgumentException("Already is registered in selector")
        keys[selector] = key
    }

    override fun unregSelector(selector: SocketSelector) {
        val g = keys[selector] ?: throw IllegalArgumentException("Not registered in selector")
        g.cancel()
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

    override fun write(data: ByteArray, offset: Int, length: Int) =
            try {
                socket.write(data = data, offset = offset, length = length)
            } catch (e: Throwable) {
                println("ERROR IN SocketChannel. =>$e")
                throw e
            }
}