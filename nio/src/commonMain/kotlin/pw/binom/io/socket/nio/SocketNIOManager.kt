package pw.binom.io.socket.nio

import pw.binom.*
import pw.binom.io.*
import pw.binom.io.socket.*
import pw.binom.concurrency.*
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

open class SocketNIOManager : Closeable {
    val threadRef = ThreadRef()

    class ReadInterruptException : RuntimeException()


    internal val selector = SocketSelector()

    val keys: Collection<SocketSelector.SelectorKey>
        get() = selector.keys

    private val executeOnThread = ArrayList<suspend (TcpConnectionRaw) -> Unit>()
    private val executeThreadLock = Lock()
    private val pop = PopResult<() -> Unit>()

    protected open fun processIo(key: SocketSelector.SelectorKey) {
        val connection = key.attachment as AbstractConnection
        try {
            if (key.isReadable) {
                connection.readyForRead()
            }
            if (key.isWritable) {
                connection.readyForWrite()
            }
        } catch (e: Throwable) {
            connection.close()
        }
    }

    protected open fun processEvent(key: SocketSelector.SelectorKey) {
        processIo(key)
    }

    @OptIn(ExperimentalTime::class)
    fun update(timeout: Int? = null) = waitEvents(timeout)

    protected fun waitEvents(timeout: Int?) {
        selector.process(timeout) { key ->
            try {
                processEvent(key)
            } catch (e: SocketClosedException) {
                //ignore disconnect event
            }
        }
    }

    /**
     * Returns clients count
     */
    val clientSize: Int
        get() = selector.keys.size

    fun bind(
        address: NetworkAddress,
        handler: ConnectHandler
    ): ServerSocketChannel {
        val channel = SocketFactory.rawSocketFactory.createSocketServerChannel()
        try {
            channel.blocking = false
            channel.bind(address)
            val connection = TcpConnectionServer(
                manager=this,
                server = channel,
                handler=handler
            )
            val key = selector.reg(channel, connection)
            key.listen(
                read = false,
                write = false,
            )
            return channel
        } catch (e: BindException) {
            channel.close()
            throw e
        }
    }

    fun bindUDP(address: NetworkAddress): DatagramConnection {
        val channel = DatagramChannel.bind(address)
        val connection = DatagramConnection(this)
        val key = selector.reg(channel, connection)
        connection.holder = UdpHolder(channel, key)
        return connection
    }

    fun openUdp(): DatagramConnection {
        val channel = DatagramChannel.open()
        val connection = DatagramConnection(this)
        val key = selector.reg(channel, connection)
        connection.holder = UdpHolder(channel, key)
        return connection
    }

    fun connect(
        address: NetworkAddress,
        attachment: Any? = null,
    ): TcpConnectionRaw {
        val channel = SocketFactory.rawSocketFactory.createSocketChannel()
        try {
            channel.connect(address)
            return attach(channel, attachment)
        } catch (e: ConnectException) {
            channel.close()
            throw e
        }
    }

    /**
     * Attach channel to current ConnectionManager
     */
    fun attach(
        channel: SocketChannel,
        attachment: Any? = null,
        func: (suspend (TcpConnectionRaw) -> Unit)? = null
    ): TcpConnectionRaw {
        executeThreadLock.synchronize {
            channel.blocking = false
            val holder = SocketHolder(channel)
            val connection = TcpConnectionRaw(attachment = attachment, holder = holder)
            val selectionKey = selector.reg(channel, connection)
            holder.selectionKey = selectionKey
            holder.doFreeze()
            selectionKey.listen(false, false)
            if (func != null)
                executeOnThread += func
            return connection
        }
    }

    override fun close() {
        selector.keys.toTypedArray().forEach {
            it.cancel()
            it.channel.close()
        }
        selector.close()
    }
}