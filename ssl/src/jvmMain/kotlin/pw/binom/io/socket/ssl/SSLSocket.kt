package pw.binom.io.socket.ssl

import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.Socket
import pw.binom.ssl.SSLContext
import java.net.Socket as JSocket
/*

actual class SSLSocket(internal val native: JSocket) : Socket {
    constructor(ctx: SSLContext) : this(ctx.ctx.socketFactory.createSocket())

    val raw = RawSocket(native)

    override val input: InputStream
        get() = raw.input
    override val output: OutputStream
        get() = raw.output

    override fun connect(host: String, port: Int) {
        raw.connect(host, port)
    }

    override val connected: Boolean
        get() = raw.connected
    override val closed: Boolean
        get() = raw.closed

    override fun close() {
        raw.close()
    }
}*/
