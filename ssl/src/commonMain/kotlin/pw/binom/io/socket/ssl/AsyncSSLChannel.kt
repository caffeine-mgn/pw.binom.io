package pw.binom.io.socket.ssl

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.empty
import pw.binom.io.AsyncChannel
import pw.binom.pool.ObjectPool

fun SSLSession.asyncChannel(
    channel: AsyncChannel,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    closeParent: Boolean = true,
) =
    AsyncSSLChannel(
        session = this,
        channel = channel,
        bufferSize = bufferSize,
        closeParent = closeParent
    )

/**
 * SSL wrapper for [channel] using [session]
 */
class AsyncSSLChannel private constructor(
    val session: SSLSession,
    val channel: AsyncChannel,
    private val pool: ObjectPool<ByteBuffer>?,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,

    /**
     * Flag for close [channel] when will called [asyncClose]
     */
    private val closeParent: Boolean,
) : AsyncChannel {

    constructor(
        session: SSLSession,
        channel: AsyncChannel,
        pool: ObjectPool<ByteBuffer>,
        closeParent: Boolean = true
    ) : this(
        session = session,
        channel = channel,
        pool = pool,
        buffer = pool.borrow().empty(),
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        session: SSLSession,
        channel: AsyncChannel,
        buffer: ByteBuffer,
        /**
         * Flag for close [channel] when will called [asyncClose]
         */
        closeParent: Boolean = true
    ) : this(
        session = session,
        channel = channel,
        pool = null,
        buffer = buffer.empty(),
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        session: SSLSession,
        channel: AsyncChannel,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        /**
         * Flag for close [channel] when will called [asyncClose]
         */
        closeParent: Boolean = true,
    ) : this(
        session = session,
        channel = channel,
        pool = null,
        buffer = ByteBuffer.alloc(bufferSize).empty(),
        closeBuffer = true,
        closeParent = closeParent,
    )

    private var eof = false

    private suspend fun sendAll() {
        while (true) {
            buffer.clear()
            val n = session.readNet(buffer)
            if (n == 0)
                break
            buffer.flip()
            channel.write(buffer)
        }
    }

    private suspend fun readAll() {
        buffer.clear()
        channel.read(buffer)
        buffer.flip()
        session.writeNet(buffer)
    }

    override suspend fun asyncClose() {
        try {
            flush()
            if (closeParent) {
                session.close()
                channel.asyncClose()
            }
        } finally {
            if (closeBuffer) {
                buffer.close()
            } else {
                pool?.recycle(buffer)
            }
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        if (eof)
            return 0
        var len = data.remaining
        val length = data.remaining
        var readed = 0
        LOOP@ while (len > 0) {
            val s = session.writeApp(data)
            len -= s.bytes
            readed += s.bytes
            sendAll()
            when (s.state) {
                SSLSession.State.WANT_WRITE -> {
                    sendAll()
                }
                SSLSession.State.WANT_READ -> {
                    readAll()
                }
                SSLSession.State.OK -> break@LOOP
                SSLSession.State.CLOSED -> {
                    eof = true
                    break@LOOP
                }
                else -> TODO("Unknown state ${s.state}")
            }
        }
        return length - len
    }

    override suspend fun flush() {
    }

    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int {
        if (eof)
            return 0
        sendAll()
        var readed = 0
        LOOP@ while (dest.remaining > 0) {
            val s = session.readApp(dest)
            readed += s.bytes
            when (s.state) {
                SSLSession.State.WANT_WRITE -> {
                    sendAll()
                }
                SSLSession.State.WANT_READ -> {
                    sendAll()
                    readAll()
                }
                SSLSession.State.OK -> {
                    if (readed > 0)
                        break@LOOP
                }
                SSLSession.State.CLOSED -> {
                    eof = true
                }
                else -> TODO("Unknown state ${s.state}")
            }
        }
        return readed
    }

}
