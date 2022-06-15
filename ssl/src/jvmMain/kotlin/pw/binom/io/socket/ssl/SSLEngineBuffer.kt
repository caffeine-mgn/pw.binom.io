package pw.binom.io.socket.ssl

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult

internal class SSLEngineBuffer(private val socketChannel: SocketChannel, private val sslEngine: SSLEngine) {

    val session = sslEngine.session

    private val networkInboundBuffer: ByteBuffer

    private val networkOutboundBuffer: ByteBuffer

    private val minimumApplicationBufferSize = session.applicationBufferSize

    private val unwrapBuffer: ByteBuffer

    private val wrapBuffer: ByteBuffer

    init {

        val networkBufferSize = session.packetBufferSize

        networkInboundBuffer = ByteBuffer.allocate(networkBufferSize)

        networkOutboundBuffer = ByteBuffer.allocate(networkBufferSize)
        networkOutboundBuffer.flip()

        unwrapBuffer = ByteBuffer.allocate(minimumApplicationBufferSize)
        wrapBuffer = ByteBuffer.allocate(minimumApplicationBufferSize)
        wrapBuffer.flip()
    }

    @Throws(IOException::class)
    fun unwrap(applicationInputBuffer: ByteBuffer): Int {
        if (applicationInputBuffer.capacity() < minimumApplicationBufferSize) {
            throw IllegalArgumentException("Application buffer size must be at least: $minimumApplicationBufferSize")
        }

        if (unwrapBuffer.position() != 0) {
            unwrapBuffer.flip()
            while (unwrapBuffer.hasRemaining() && applicationInputBuffer.hasRemaining()) {
                applicationInputBuffer.put(unwrapBuffer.get())
            }
            unwrapBuffer.compact()
        }

        var totalUnwrapped = 0
        var unwrapped: Int
        var wrapped: Int

        do {
            unwrapped = doUnwrap(applicationInputBuffer)
            totalUnwrapped += unwrapped
            wrapped = doWrap(wrapBuffer)
        } while (unwrapped > 0 || wrapped > 0 && networkOutboundBuffer.hasRemaining() && networkInboundBuffer.hasRemaining())

        return totalUnwrapped
    }

    @Throws(IOException::class)
    fun wrap(applicationOutboundBuffer: ByteBuffer): Int {
        val wrapped = doWrap(applicationOutboundBuffer)
        doUnwrap(unwrapBuffer)
        return wrapped
    }

    @Throws(IOException::class)
    fun flushNetworkOutbound(): Int {
        return send(socketChannel, networkOutboundBuffer)
    }

    @Throws(IOException::class)
    fun send(channel: SocketChannel, buffer: ByteBuffer): Int {
        var totalWritten = 0
        while (buffer.hasRemaining()) {
            val written = channel.write(buffer)

            if (written == 0) {
                break
            } else if (written < 0) {
                return if (totalWritten == 0) written else totalWritten
            }
            totalWritten += written
        }
        return totalWritten
    }

    fun close() {
        try {
            sslEngine.closeInbound()
        } catch (e: Exception) {
        }

        try {
            sslEngine.closeOutbound()
        } catch (e: Exception) {
        }
    }

    @Throws(IOException::class)
    private fun doUnwrap(applicationInputBuffer: ByteBuffer): Int {

        var totalReadFromChannel = 0

        // Keep looping until peer has no more data ready or the applicationInboundBuffer is full
        UNWRAP@ do {
            // 1. Pull data from peer into networkInboundBuffer

            var readFromChannel = 0
            while (networkInboundBuffer.hasRemaining()) {
                val read = socketChannel.read(networkInboundBuffer)
                if (read <= 0) {
                    if (read < 0 && readFromChannel == 0 && totalReadFromChannel == 0) {
                        // No work done and we've reached the end of the channel from peer
                        return read
                    }
                    break
                } else {
                    readFromChannel += read
                }
            }

            networkInboundBuffer.flip()
            if (!networkInboundBuffer.hasRemaining()) {
                networkInboundBuffer.compact()
                // wrap(applicationOutputBuffer, applicationInputBuffer, false);
                return totalReadFromChannel
            }

            totalReadFromChannel += readFromChannel

            try {
                val result = sslEngine.unwrap(networkInboundBuffer, applicationInputBuffer)

                when (result.status!!) {
                    SSLEngineResult.Status.OK -> {
                        val handshakeStatus = result.handshakeStatus
                        when (handshakeStatus) {
                            SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> {
                            }

                            SSLEngineResult.HandshakeStatus.NEED_WRAP -> break@UNWRAP

                            SSLEngineResult.HandshakeStatus.NEED_TASK -> runHandshakeTasks()

                            SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING -> {
                            }
                            else -> {
                            }
                        }
                    }

                    SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                        break@UNWRAP
                    }

                    SSLEngineResult.Status.CLOSED -> {
                        return if (totalReadFromChannel == 0) -1 else totalReadFromChannel
                    }

                    SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    }
                }
            } finally {
                networkInboundBuffer.compact()
            }
        } while (applicationInputBuffer.hasRemaining())

        applicationInputBuffer.limit(applicationInputBuffer.position() + totalReadFromChannel)
        return totalReadFromChannel
    }

    @Throws(IOException::class)
    private fun doWrap(applicationOutboundBuffer: ByteBuffer): Int {
        var totalWritten = 0

        // 1. Send any data already wrapped out channel

        if (networkOutboundBuffer.hasRemaining()) {
            totalWritten = send(socketChannel, networkOutboundBuffer)
            if (totalWritten < 0) {
                return totalWritten
            }
        }

        // 2. Any data in application buffer ? Wrap that and send it to peer.

        WRAP@ while (true) {
            networkOutboundBuffer.compact()
            val result = sslEngine.wrap(applicationOutboundBuffer, networkOutboundBuffer)

            networkOutboundBuffer.flip()
            if (networkOutboundBuffer.hasRemaining()) {
                val written = send(socketChannel, networkOutboundBuffer)
                if (written < 0) {
                    return if (totalWritten == 0) written else totalWritten
                } else {
                    totalWritten += written
                }
            }

            when (result.status) {
                SSLEngineResult.Status.OK -> when (result.handshakeStatus) {
                    SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    }

                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN,
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> break@WRAP

                    SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                        runHandshakeTasks()
                    }

                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING -> if (applicationOutboundBuffer.hasRemaining()) {
                    } else {
                        break@WRAP
                    }
                    SSLEngineResult.HandshakeStatus.FINISHED -> break@WRAP
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    break@WRAP
                }

                SSLEngineResult.Status.CLOSED -> {
                    break@WRAP
                }

                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    break@WRAP
                }
            }
        }

        return totalWritten
    }

    private fun runHandshakeTasks() {
        while (true) {
            val runnable = sslEngine.delegatedTask
            if (runnable == null) {
                break
            } else {
                runnable.run()
//                executorService.execute(runnable)
            }
        }
    }
}
