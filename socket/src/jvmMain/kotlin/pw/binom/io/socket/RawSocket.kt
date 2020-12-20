package pw.binom.io.socket

import pw.binom.ByteBuffer
import pw.binom.io.*
import java.net.InetSocketAddress
import java.net.Socket as JSocket

actual class RawSocket constructor(val native: JSocket) : Socket {

    actual constructor() : this(JSocket())

    override fun close() {
        native.close()
    }

    override fun write(data: ByteBuffer): Int =
            native.channel.write(data.native)

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset,length){data->
//            native.channel.write(data)
//        }
//    }

    override fun flush() {
    }

//    override fun skip(length: Long): Long {
//        var l = length
//        while (l > 0) {
//            skipBuffer.reset(0, minOf(skipBuffer.capacity, l.toInt()))
//            l -= read(skipBuffer)
//        }
//        return length
//    }

    override fun read(dest: ByteBuffer): Int =
            native.channel.read(dest.native)

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset,length){data->
//            native.channel.read(data)
//        }
//    }

    override fun connect(host: String, port: Int) {
        try {
            native.connect(InetSocketAddress(host, port))
        } catch (e: java.net.UnknownHostException) {
            throw UnknownHostException(e.message!!)
        } catch (e: java.net.ConnectException) {
            throw ConnectException(e.message)
        }
    }

    override val connected: Boolean
        get() = native.isConnected && !closed

    override val closed: Boolean
        get() = native.isClosed


}

//private val skipBuffer = ByteBuffer.alloc(128)