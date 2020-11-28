package pw.binom.io.socket.ssl

import pw.binom.io.socket.NetworkChannel
import pw.binom.io.socket.SocketClosedException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession
import java.nio.channels.SocketChannel as JSocketChannel
/*

*/
/**
 * A wrapper around a real [SocketChannel] that adds SSL support.
 *//*

actual class SSLSocketChannel
*/
/**
 *
 * @param socketChannel The real SocketChannel.
 * @param sslEngine The SSL engine to use for traffic back and forth on the given SocketChannel.
 * @param executorService Used to execute long running, blocking SSL operations such as certificate validation with a CA ([NEED_TASK](http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLEngineResult.HandshakeStatus.html#NEED_TASK))
 * @param log The logger for debug and error messages. A `null` logger will result in no log operations.
 * @throws IOException
 *//*

(val wrappedSocketChannel: JSocketChannel, sslEngine: SSLEngine) : pw.binom.io.socket.SocketChannel, NetworkChannel {
    //val wrappedSocketChannel=SocketChannel.open()

    private val sslEngineBuffer: SSLEngineBuffer

    override var blocking: Boolean
        get() = wrappedSocketChannel.isBlocking
        set(value) {
            wrappedSocketChannel.configureBlocking(value)
        }
    override val isConnected: Boolean
        get() = wrappedSocketChannel.isConnected
    override val native: JSocketChannel
        get() = wrappedSocketChannel

    init {

        sslEngineBuffer = SSLEngineBuffer(wrappedSocketChannel, sslEngine)
    }

    */
/**
     *
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     *
     * An attempt is made to read up to r bytes from the channel, where r is the number of bytes remaining in the buffer, that is, dst.remaining(), at the moment this method is invoked.
     *
     *
     * Suppose that a byte sequence of length n is read, where 0 <= n <= r. This byte sequence will be transferred into the buffer so that the first byte in the sequence is at index p and the last byte is at index p + n - 1, where p is the buffer's position at the moment this method is invoked. Upon return the buffer's position will be equal to p + n; its limit will not have changed.
     *
     *
     * A read operation might not fill the buffer, and in fact it might not read any bytes at all. Whether or not it does so depends upon the nature and state of the channel. A socket channel in non-blocking mode, for example, cannot read any more bytes than are immediately available from the socket's input buffer; similarly, a file channel cannot read any more bytes than remain in the file. It is guaranteed, however, that if a channel is in blocking mode and there is at least one byte remaining in the buffer then this method will block until at least one byte is read.
     *
     *
     * This method may be invoked at any time. If another thread has already initiated a read operation upon this channel, however, then an invocation of this method will block until the first operation is complete.
     *
     * @param applicationBuffer The buffer into which bytes are to be transferred
     * @return The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream
     * @throws java.nio.channels.NotYetConnectedException If this channel is not yet connected
     * @throws java.nio.channels.ClosedChannelException If this channel is closed
     * @throws java.nio.channels.AsynchronousCloseException If another thread closes this channel while the read operation is in progress
     * @throws java.nio.channels.ClosedByInterruptException If another thread interrupts the current thread while the read operation is in progress, thereby closing the channel and setting the current thread's interrupt status
     * @throws IOException If some other I/O error occurs
     * @throws IllegalArgumentException If the given applicationBuffer capacity ([ByteBuffer.capacity] is less then the application buffer size of the [SSLEngine] session application buffer size ([SSLSession.getApplicationBufferSize] this channel was constructed was.
     *//*

    @Synchronized
    @Throws(IOException::class, IllegalArgumentException::class)
    fun read(applicationBuffer: ByteBuffer): Int {
        try {
            val intialPosition = applicationBuffer.position()

            val readFromChannel = sslEngineBuffer.unwrap(applicationBuffer)

            if (readFromChannel < 0) {
                return readFromChannel
            } else {
                val totalRead = applicationBuffer.position() - intialPosition
                return totalRead
            }
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            throw SSLHandshakeException(e.message, e.cause)
        }
    }

    val readBuf = ByteBuffer.allocate(sslEngine.session.applicationBufferSize).limit(0)
    override val available: Int
        get() {
            if (!readBuf.hasRemaining())
                return -1
            return readBuf.remaining()
        }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (data.size < offset + length)
            throw IndexOutOfBoundsException()
        if (length == 0) {
            return 0
        }
        if (!readBuf.hasRemaining()) {
            readBuf.clear()
            val r = read(readBuf)
            if (r == -1)
                return -1
            readBuf.limit(r)
            if (r <= 0)
                return r
            readBuf.flip()
        }
        val l = minOf(length, readBuf.remaining())
        readBuf.get(data, offset, l)
        return l
    }

    */
/**
     *
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     *
     * An attempt is made to write up to r bytes to the channel, where r is the number of bytes remaining in the buffer, that is, src.remaining(), at the moment this method is invoked.
     *
     *
     * Suppose that a byte sequence of length n is written, where 0 <= n <= r. This byte sequence will be transferred from the buffer starting at index p, where p is the buffer's position at the moment this method is invoked; the index of the last byte written will be p + n - 1. Upon return the buffer's position will be equal to p + n; its limit will not have changed.
     *
     *
     * Unless otherwise specified, a write operation will return only after writing all of the r requested bytes. Some types of channels, depending upon their state, may write only some of the bytes or possibly none at all. A socket channel in non-blocking mode, for example, cannot write any more bytes than are free in the socket's output buffer.
     *
     *
     * This method may be invoked at any time. If another thread has already initiated a write operation upon this channel, however, then an invocation of this method will block until the first operation is complete.
     *
     * @param applicationBuffer The buffer from which bytes are to be retrieved
     * @return The number of bytes written, possibly zero
     * @throws java.nio.channels.NotYetConnectedException If this channel is not yet connected
     * @throws java.nio.channels.ClosedChannelException If this channel is closed
     * @throws java.nio.channels.AsynchronousCloseException If another thread closes this channel while the read operation is in progress
     * @throws java.nio.channels.ClosedByInterruptException If another thread interrupts the current thread while the read operation is in progress, thereby closing the channel and setting the current thread's interrupt status
     * @throws IOException If some other I/O error occurs
     * @throws IllegalArgumentException If the given applicationBuffer capacity ([ByteBuffer.capacity] is less then the application buffer size of the [SSLEngine] session application buffer size ([SSLSession.getApplicationBufferSize] this channel was constructed was.
     *//*

    @Synchronized
    @Throws(IOException::class, IllegalArgumentException::class)
    fun write(applicationBuffer: ByteBuffer): Int {
        if (!isConnected)
            throw SocketClosedException()
        try {
            val intialPosition = applicationBuffer.position()
            val writtenToChannel = sslEngineBuffer.wrap(applicationBuffer)

            if (writtenToChannel < 0) {
                return writtenToChannel
            } else {
                val totalWritten = applicationBuffer.position() - intialPosition
                return totalWritten
            }
        } catch (e: IOException) {
            throw pw.binom.io.IOException(e.message, e.cause)
        }
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int =
            try {
                write(ByteBuffer.wrap(data, offset, length))
            } catch (e: javax.net.ssl.SSLHandshakeException) {
                throw SSLHandshakeException(e.message, e.cause)
            }

    override fun connect(host: String, port: Int) {
        wrappedSocketChannel.connect(InetSocketAddress(host, port))
    }

    override fun flush() {

    }

    @Throws(IOException::class)
    fun finishConnect(): Boolean {
        return wrappedSocketChannel.finishConnect()
    }

    @Throws(IOException::class)
    fun bind(local: SocketAddress): SSLSocketChannel {
        wrappedSocketChannel.bind(local)
        return this
    }

    @Throws(IOException::class)
    fun getLocalAddress(): SocketAddress {
        return wrappedSocketChannel.localAddress
    }


    @Throws(IOException::class)
    fun shutdownInput(): JSocketChannel {
        return wrappedSocketChannel.shutdownInput()
    }

    @Throws(IOException::class)
    fun shutdownOutput(): JSocketChannel {
        return wrappedSocketChannel.shutdownOutput()
    }


    @Throws(IOException::class)
    fun implCloseSelectableChannel() {
        try {
            sslEngineBuffer.flushNetworkOutbound()
        } catch (e: Exception) {
        }

        wrappedSocketChannel.close()
        sslEngineBuffer.close()
    }

    override fun close() {
        implCloseSelectableChannel()
    }
}*/
