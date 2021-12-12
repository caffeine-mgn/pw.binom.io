package pw.binom.mq.nats.client

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.ByteBuffer
import pw.binom.io.AsyncCloseable
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcher
import pw.binom.network.SocketConnectException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class NatsConnectorImpl(
    val clientName: String? = null,
    val lang: String = "kotlin",
    val echo: Boolean = true,
    val tlsRequired: Boolean = false,
    val user: String? = null,
    val pass: String? = null,
    val defaultGroup: String? = null,
    val attemptCount: Int = 3,
    var networkDispatcher: NetworkCoroutineDispatcher,
    serverList: List<NetworkAddress>
) : NatsConnector {
    init {
        require(serverList.isNotEmpty())
        require(attemptCount >= 1)
    }

    class Subscribe(
        val subscribeId: String,
        val subject: String,
        val group: String?,
    )

    private var closed = false
    private val subscribes = HashMap<String, Subscribe>()
    private var connection: NatsRawConnection? = null
    private var _serverList = ArrayList(serverList)
    val serverList: List<NetworkAddress>
        get() = _serverList
    var serverIndex = 0

    /**
     * Creates new subscribe and returns [AsyncCloseable] for abort subscribe
     *
     * @param subject name of topic
     * @param group group for subscribe. Default value is [defaultGroup]
     */
    override suspend fun subscribe(
        subject: String,
        group: String?,
        subscribeId: String,
    ): AsyncCloseable {
        checkClosed()
        checkConnection().subscribe(
                subscribeId = subscribeId,
                subject = subject,
                group = group,
        )
        subscribes[subscribeId] = Subscribe(
                subscribeId = subscribeId,
                subject = subject,
                group = group,
        )

        return AsyncCloseable {
            checkClosed()
            if (subscribes.remove(subscribeId) != null) {
                checkConnection().unsubscribe(id = subscribeId)
            }
        }
    }

    override suspend fun readMessage(): NatsMessage {
        return checkConnection().readMessage()
    }

    private var connecting = false
    private var connectionWaters = ArrayList<CancellableContinuation<NatsRawConnection>>()

    private suspend fun checkConnection(): NatsRawConnection {
        if (connection != null) {
            return connection!!
        }
        if (connecting) {
            return suspendCancellableCoroutine { connectionWaters.add(it) }
        }
        connecting = true
        try {
            var attemptCount = attemptCount
            val startIndex = serverIndex
            var first = true
            CONNECT_LOOP@ while (true) {
                if (!first && startIndex == serverIndex) {
                    attemptCount--
                }
                if (attemptCount == -1) {
                    connectionWaters.forEach {
                        it.resumeWithException(SocketConnectException())
                    }
                    connectionWaters.clear()
                    throw SocketConnectException()
                }
                first = false
                if (serverIndex >= serverList.size) {
                    serverIndex = 0
                }
                val addr = serverList[serverIndex]
                val tcpConnection = try {
                    println("Connecting to $addr")
                    val connection = networkDispatcher.tcpConnect(addr)
                    println("Connected to $addr")
                    connection
                } catch (e: SocketConnectException) {
                    println("Can't connect to $addr")
                    serverIndex++
                    continue@CONNECT_LOOP
                }
                try {
                    val connect = NatsRawConnection(
                            channel = tcpConnection,
                    )
                    val c = connect.prepareConnect(
                            clientName = clientName,
                            lang = lang,
                            echo = echo,
                            tlsRequired = tlsRequired,
                            user = user,
                            pass = pass
                    )
                    val list = (serverList + c.clusterAddresses).distinct()
                    _serverList.clear()
                    _serverList.addAll(list)
                    subscribes.values.forEach {
                        connect.subscribe(
                                subscribeId = it.subscribeId,
                                subject = it.subject,
                                group = it.group
                        )
                    }
                    this.connection = connect
                    connectionWaters.forEach {
                        it.resume(connect)
                    }
                    connectionWaters.clear()
                    return connect
                } catch (e: Throwable) {
                    serverIndex++
                    continue@CONNECT_LOOP
                }
            }
        } finally {
            connecting = false
        }
    }

    private fun checkClosed() {
        check(!closed) { "Connection already closed" }
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        connection?.asyncClose()
        connection = null
        subscribes.clear()
    }

    override suspend fun publish(subject: String, replyTo: String?, data: ByteArray?) {
        checkConnection().publish(
                subject = subject,
                replyTo = replyTo,
                data = data
        )
    }

    override suspend fun publish(subject: String, replyTo: String?, data: ByteBuffer?) {
        checkConnection().publish(
                subject = subject,
                replyTo = replyTo,
                data = data
        )
    }
}