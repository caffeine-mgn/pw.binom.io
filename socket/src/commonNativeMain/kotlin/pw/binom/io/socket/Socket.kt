package pw.binom.io.socket

import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.*

actual class Socket internal constructor(internal val native: NativeSocketHolder) : Closeable, InputStream, OutputStream {


    private var _connected = AtomicBoolean(false)

    private var _closed = AtomicBoolean(false)

    private val _blocking= AtomicBoolean(false)

    actual val connected: Boolean
        get() = _connected.value

    var blocking: Boolean
        get()=_blocking.value
        set(value) {
            setBlocking(native, value)
            _blocking.value = value
        }

    actual val closed: Boolean
        get() = _closed.value

    actual constructor() : this(initNativeSocket())

    init {
        doFreeze()
    }

    internal fun internalDisconnected() {
        _connected.value = false
    }

    internal fun internalConnected() {
        _connected.value = true
    }

    override fun flush() {
        //NOP
    }

    override fun close() {
        closeSocket(native)

        _closed.value = true
        internalDisconnected()
    }


    fun bind(host:String,port: Int) {
        if (connected)
            throw SocketException("Socket already connected")
        portCheck(port)

        bindSocket(native, host, port)
        internalConnected()
    }

    actual fun connect(host: String, port: Int) {

        portCheck(port)

        connectSocket(native, host, port)
        internalConnected()
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val r = recvSocket(native, data, offset, length)
        if (r <= 0) {
            close()
            throw SocketClosedException()
        }
        return r
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (closed)
            throw SocketClosedException()
        if (!connected)
            throw IOException("Socket is not connected")

        if (length == 0)
            return 0
        if (offset + length > data.size)
            throw IllegalArgumentException("Array Index Out Of Bounds Exception")
        sendSocket(native, data, offset, length)
        return length
    }


}

internal fun portCheck(port: Int) {
    if (port < 0 || port > 0xFFFF)
        throw IllegalArgumentException("port out of range:$port")
}