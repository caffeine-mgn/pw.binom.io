package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.inet_pton
import platform.posix.*
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

actual class Socket constructor(internal val native: Int) : Closeable, InputStream, OutputStream {
    override fun flush() {
//
    }

    actual constructor() : this(socket(AF_INET, SOCK_STREAM, 0))

    override fun close() {
        close(native)
    }

    private var _connected = false

    actual val connected: Boolean
        get() = _connected

    fun bind(port: Int) {
        memScoped {
            val serverAddr = alloc<sockaddr_in>()
            with(serverAddr) {
                memset(this.ptr, 0, sockaddr_in.size.convert())
                sin_family = AF_INET.convert()
                //sin_addr.s_addr = posix_htons(0).convert()//TODO что тут в линуксе
                sin_port = posix_htons(port.convert()).convert()
            }
            if (bind(native, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert()) != 0)
                throw IOException()
            if (listen(native, SOMAXCONN) == -1)
                throw IOException()
            _connected = true
        }
    }

    internal fun setConnected() {
        _connected = true
    }

    actual fun connect(host: String, port: Int) {
        memScoped {
            val addr = alloc<sockaddr_in>()
            memset(addr.ptr, 0, sizeOf<sockaddr_in>().convert())
            addr.sin_family = AF_INET.convert()
            addr.sin_port = htons(port.toUShort())

            if (inet_pton(AF_INET, host, addr.sin_addr.ptr) <= 0) {
                val server = gethostbyname(host) ?: throw SocketConnectException("Unknown host $host")
                addr.sin_addr.s_addr = server!!.pointed.h_addr_list!![0]!!.reinterpret<UIntVar>().pointed.value
            }

            val r = connect(native, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
            if (r < 0) {
                throw SocketConnectException("Can't connect to $host:$port ($r)")
            }
            _connected = true
        }
    }

    fun read(buf: CValuesRef<ByteVar>, length: Int): Int {
        if (!connected)
            throw SocketClosedException()
        val r = recv(native, buf, length.convert(), 0).convert<Int>()
        if (r == 0) {
            _connected = false
            throw SocketClosedException()
        }
        return r
    }

    fun write(data: CValuesRef<ByteVar>, length: Int): Int {
        if (!connected)
            throw SocketClosedException()
        val r = send(native, data, length.convert(), 0).convert<Int>()
        if (r == 0) {
            _connected = false
            throw SocketClosedException()
        }
        return r
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int = read(data.refTo(offset), length)

    override fun write(data: ByteArray, offset: Int, length: Int): Int = write(data.refTo(offset), length)

    var blocking: Boolean
        get() = fcntl(native, F_GETFL, 0) and O_NONBLOCK == 0
        set(value) {
            val flags = fcntl(native, F_GETFL, 0)
            val newFlags = if (value)
                flags xor O_NONBLOCK
            else
                flags or O_NONBLOCK

            if (0 != fcntl(native, F_SETFL, newFlags))
                throw IOException()
        }

    actual companion object {
        actual fun startup() {
        }

        actual fun shutdown() {
        }
    }
}

private fun hostToIp(host: String) {

}