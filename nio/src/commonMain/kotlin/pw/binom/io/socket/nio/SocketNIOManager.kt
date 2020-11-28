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
//    fun interface ConnectHandler {
//        fun clientConnected(connection: TcpConnectionRaw, manager: SocketNIOManager)
//    }


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
//
//    protected open fun processIo1(key: SocketSelector.SelectorKey) {
//        val client = (key.attachment as TcpConnectionRaw?) ?: return
//        try {
//            if (client.detached) {
//                return
//            }
//
//            if (client.holder.selectionKey.isCanlelled) {
//                return
//            }
//            if (client.detached)
//                return
//
//            val readReadyCallback = client.holder.readyForReadListener
//            if (key.isReadable && readReadyCallback != null) {
//                client.holder.readyForReadListener.pop(pop)
//                if (!pop.isEmpty) {
//                    try {
//                        pop.value()
//                    } catch (e: Throwable) {
//                        client.readSchedule?.finish(Result.failure(e))
//                        client.writeSchedule?.finish(Result.failure(e))
//                        client.readSchedule = null
//                        client.writeSchedule = null
//                        key.listen(false, false)
//                        client.detach().close()
//                        throw e
//                    }
//                    return
//                }
//            }
//
//            if (key.isWritable) {
//                client.holder.readyForWriteListener.pop(pop)
//                if (!pop.isEmpty) {
//                    try {
//                        pop.value()
//                    } catch (e: Throwable) {
//                        client.readSchedule?.finish(Result.failure(e))
//                        client.writeSchedule?.finish(Result.failure(e))
//                        client.readSchedule = null
//                        client.writeSchedule = null
//                        key.listen(false, false)
//                        client.detach().close()
//                        throw e
//                    }
//                    return
//                }
//            }
//
//            val writeWait = client.writeSchedule
//
//            if (key.isWritable && writeWait != null) {
//                val result = runCatching { client.holder.channel.write(writeWait.buffer) }
//                if (result.isFailure) {
//                    client.writeSchedule = null
//                    writeWait.finish(result)
//                } else {
//                    if (writeWait.buffer.remaining == 0) {
//                        client.writeSchedule = null
//                        writeWait.finish(result)
//                    }
//                }
//                return
//            }
//
//            val readWait = client.readSchedule
//            if (key.isReadable && readWait != null) {
//                client.readSchedule = null
//                readWait.finish(runCatching { client.holder.channel.read(readWait.buffer) })
//                return
//            }
//
//            if (!client.holder.channel.isConnected) {
//                key.cancel()
//                return
//            }
//
//            if (!key.isCanlelled)
//                key.listen(
//                    client.readSchedule != null || !client.holder.readyForReadListener.isEmpty,
//                    client.writeSchedule != null || !client.holder.readyForWriteListener.isEmpty
//                )
//        } catch (e: Throwable) {
//            key.listen(false, false)
//            client.readSchedule?.finish(Result.failure(e))
//            client.writeSchedule?.finish(Result.failure(e))
//            client.readSchedule = null
//            client.writeSchedule = null
//            client.detach().close()
//            throw e
//        }
//    }

//    protected open fun processAccept(key: SocketSelector.SelectorKey) {
//        val server = key.channel as ServerSocketChannel
//        val cl = server.accept() ?: return@processAccept
//        val holder = SocketHolder(cl)
//        val connection = TcpConnectionRaw(attachment = null, holder = holder)
//        cl.blocking = false
//        val selectionKey = selector.reg(cl, connection)
//        holder.selectionKey = selectionKey
//        holder.doFreeze()
//        val handler = key.attachment as ConnectHandler?
//        handler?.connected(connection)
//    }

    protected open fun processEvent(key: SocketSelector.SelectorKey) {
//        if (key.channel is ServerSocketChannel) {
//            processAccept(key)
//        } else {
//
//        }
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

    fun findClient(func: (TcpConnectionRaw) -> Boolean): TcpConnectionRaw? =
        selector.keys.asSequence().map { it.attachment as? TcpConnectionRaw }.filterNotNull().find(func)

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
//            val con = (it.attachment as? ConnectionRaw) ?: return@forEach
//            if (con.manager === this) {
//                async {
//                    con.close()
//                }
//            }
        }
        selector.close()
    }
}