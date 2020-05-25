package pw.binom.io.socket.ssl

import pw.binom.io.socket.NetworkChannel
import java.io.IOException
import java.net.*
import java.nio.channels.ServerSocketChannel
import java.util.*
import javax.net.ssl.SSLEngine

/**
 *
 * A wrapper around a real [ServerSocketChannel] that produces [SSLSocketChannel] on [.accept]. The real ServerSocketChannel must be
 * bound externally (to this class) before calling the accept method.
 *
 * @see SSLSocketChannel
 */
actual class SSLServerSocketChannel
/**
 *
 * @param serverSocketChannel The real server socket channel that accepts network requests.
 * @param sslContext The SSL context used to create the [SSLEngine] for incoming requests.
 * @param threadPool The thread pool passed to SSLSocketChannel used to execute long running, blocking SSL operations such as certificate validation with a CA ([NEED_TASK](http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLEngineResult.HandshakeStatus.html#NEED_TASK))
 * @param logger The logger for debug and error messages. A null logger will result in no log operations.
 */
(private val sslContext: pw.binom.ssl.SSLContext) : pw.binom.io.socket.ServerSocketChannel, NetworkChannel {
    override fun bind(host: String, port: Int) {
        serverSocketChannel.bind(InetSocketAddress(host,port))
    }

    override var blocking: Boolean
        get() = serverSocketChannel.isBlocking
        set(value) {
            serverSocketChannel.configureBlocking(value)
        }
    override val native: ServerSocketChannel
        get() = serverSocketChannel

    override fun close() {
        serverSocketChannel.close()
    }

    val serverSocketChannel=ServerSocketChannel.open()
    /**
     * Should the SSLSocketChannels created from the accept method be put in blocking mode. Default is `false`.
     */
    var blockingMode: Boolean = false

    /**
     * Should the SS server ask for client certificate authentication? Default is `false`.
     */
    var wantClientAuthentication: Boolean = false

    /**
     * Should the SSL server require client certificate authentication? Default is `false`.
     */
    var needClientAuthentication: Boolean = false

    /**
     * The list of SSL protocols (TLSv1, TLSv1.1, etc.) supported for the SSL exchange. Default is the JVM default.
     */
    var includedProtocols: List<String>? = null

    /**
     * A list of SSL protocols (SSLv2, SSLv3, etc.) to explicitly exclude for the SSL exchange. Default is none.
     */
    var excludedProtocols: List<String>? = null

    /**
     * The list of ciphers allowed for the SSL exchange. Default is the JVM default.
     */
    var includedCipherSuites: List<String>? = null

    /**
     * A list of ciphers to explicitly exclude for the SSL exchange. Default is none.
     */
    var excludedCipherSuites: List<String>? = null

    /**
     * Convenience call to keep from having to cast `SocketChannel` into [SSLSocketChannel] when calling [.accept].
     *
     * @return An SSLSocketChannel or `null` if this channel is in non-blocking mode and no connection is available to be accepted.
     * @see .accept
     */
    @Throws(IOException::class)
    fun acceptOverSSL(): SSLSocketChannel? {
        return accept() as SSLSocketChannel?
    }

    /**
     *
     * Accepts a connection made to this channel's socket.
     *
     *
     * If this channel is in non-blocking mode then this method will immediately return null if there are no pending connections. Otherwise it will block indefinitely until a new connection is available or an I/O error occurs.
     *
     *
     * The socket channel returned by this method, if any, will be in blocking mode regardless of the blocking mode of this channel.
     *
     *
     * This method performs exactly the same security checks as the accept method of the ServerSocket class. That is, if a security manager has been installed then for each new connection this method verifies that the address and port number of the connection's remote endpoint are permitted by the security manager's checkAccept method.
     *
     * @return An SSLSocketChannel or `null` if this channel is in non-blocking mode and no connection is available to be accepted.
     * @throws java.nio.channels.NotYetConnectedException If this channel is not yet connected
     * @throws java.nio.channels.ClosedChannelException If this channel is closed
     * @throws java.nio.channels.AsynchronousCloseException If another thread closes this channel while the read operation is in progress
     * @throws java.nio.channels.ClosedByInterruptException If another thread interrupts the current thread while the read operation is in progress, thereby closing the channel and setting the current thread's interrupt status
     * @throws IOException If some other I/O error occurs
     */
    @Throws(IOException::class)
    override fun accept(): SSLSocketChannel? {
        val channel = serverSocketChannel.accept()
        if (channel == null) {
            return null
        } else {
            channel.configureBlocking(blockingMode)

            val sslEngine = sslContext.ctx.createSSLEngine()
            sslEngine.useClientMode = false
            sslEngine.wantClientAuth = false//wantClientAuthentication
            sslEngine.needClientAuth = false//needClientAuthentication
            sslEngine.enabledProtocols = filterArray(sslEngine.enabledProtocols, includedProtocols, excludedProtocols)
            sslEngine.enabledCipherSuites = filterArray(sslEngine.enabledCipherSuites, includedCipherSuites, excludedCipherSuites)

            return SSLSocketChannel(channel, sslEngine)
        }
    }

//    @Throws(IOException::class)
//    override fun bind(local: SocketAddress, backlog: Int): ServerSocketChannel {
//        return serverSocketChannel.bind(local, backlog)
//    }
//
//    @Throws(IOException::class)
//    override fun getLocalAddress(): SocketAddress {
//        return serverSocketChannel.localAddress
//    }
//
//    @Throws(IOException::class)
//    override fun <T> setOption(name: SocketOption<T>, value: T): ServerSocketChannel {
//        return serverSocketChannel.setOption(name, value)
//    }
//
//    @Throws(IOException::class)
//    override fun <T> getOption(name: SocketOption<T>): T {
//        return serverSocketChannel.getOption(name)
//    }
//
//    override fun supportedOptions(): Set<SocketOption<*>> {
//        return serverSocketChannel.supportedOptions()
//    }
//
//    override fun socket(): ServerSocket {
//        return serverSocketChannel.socket()
//    }
//
//    @Throws(IOException::class)
//    override fun implCloseSelectableChannel() {
//        serverSocketChannel.close()
//    }
//
//    @Throws(IOException::class)
//    override fun implConfigureBlocking(b: Boolean) {
//        serverSocketChannel.configureBlocking(b)
//    }

    companion object {

        internal fun filterArray(items: Array<String>?, includedItems: List<String>?, excludedItems: List<String>?): Array<String> {
            val filteredItems = if (items == null) ArrayList() else Arrays.asList(*items)
            if (includedItems != null) {
                for (i in filteredItems.indices.reversed()) {
                    if (!includedItems.contains(filteredItems[i])) {
                        filteredItems.removeAt(i)
                    }
                }

                for (includedProtocol in includedItems) {
                    if (!filteredItems.contains(includedProtocol)) {
                        filteredItems.add(includedProtocol)
                    }
                }
            }

            if (excludedItems != null) {
                for (i in filteredItems.indices.reversed()) {
                    if (excludedItems.contains(filteredItems[i])) {
                        filteredItems.removeAt(i)
                    }
                }
            }

            return filteredItems.toTypedArray()
        }
    }
}