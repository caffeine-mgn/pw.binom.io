package pw.binom.io.socket

import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.IOException
import pw.binom.io.SocketException

actual class RawSocket internal constructor(override val native: NativeSocketHolder) : Socket {

    private var _connected = AtomicBoolean(false)

    private var _closed = AtomicBoolean(false)

    private val _blocking = AtomicBoolean(false)

    override val connected: Boolean
        get() = _connected.value

    var blocking: Boolean
        get() = _blocking.value
        set(value) {
            setBlocking(native, value)
            _blocking.value = value
        }

    override val closed: Boolean
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

    override fun close() {
        closeSocket(native)

        _closed.value = true
        internalDisconnected()
    }

    override fun write(data: ByteBuffer): Int =
            sendSocket(native, data)

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            sendSocket(native, data, offset, length)

    override fun flush() {
    }

//    override fun skip(length: Long): Long {
//        var l = length
//        while (l > 0) {
//            skipBuffer.reset(0, l.toInt())
//            l -= read(skipBuffer)
//        }
//        return length
//    }

    override fun read(dest: ByteBuffer): Int {
        val r = recvSocket(native, dest)
        if (r <= 0) {
            this@RawSocket.close()
            throw SocketClosedException()
        }
        return r
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val r = recvSocket(native, data, offset, length)
//        if (r <= 0) {
//            this@RawSocket.close()
//            throw SocketClosedException()
//        }
//        return r
//    }


    fun bind(host: String, port: Int) {
        if (connected)
            throw SocketException("Socket already connected")
        portCheck(port)

        bindSocket(native, host, port)
        internalConnected()
    }

    override fun connect(host: String, port: Int) {

        portCheck(port)

        connectSocket(native, host, port)
        internalConnected()
    }
}

//private val skipBuffer = ByteBuffer.alloc(128)

internal fun portCheck(port: Int) {
    if (port < 0 || port > 0xFFFF)
        throw IllegalArgumentException("port out of range:$port")
}