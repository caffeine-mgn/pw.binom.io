package pw.binom.mq.nats.client

import pw.binom.ByteBuffer
import pw.binom.UUID
import pw.binom.io.AsyncCloseable
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.SocketConnectException
import pw.binom.nextUuid
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

class NatsConnector(
    val clientName: String? = null,
    val lang: String = "kotlin",
    val echo: Boolean = true,
    val tlsRequired: Boolean = false,
    val user: String? = null,
    val pass: String? = null,
    val defaultGroup: String? = null,
    val attemptCount: Int = 3,
    var networkDispatcher: NetworkDispatcher,
    serverList: List<NetworkAddress>
) : AsyncCloseable {
    init {
        require(serverList.isNotEmpty())
        require(attemptCount >= 1)
    }

    class Subscribe(
        val subscribeId: UUID,
        val subject: String,
        val group: String?,
    )

    private var closed = false
    private val subscribes = HashMap<UUID, Subscribe>()
    private var connection: NatsRawConnection? = null
    private var _serverList = ArrayList<NetworkAddress>(serverList)
    val serverList: List<NetworkAddress>
        get() = _serverList
    var serverIndex = 0

    /**
     * Creates new subscribe and returns [AsyncCloseable] for abort subscribe
     *
     * @param subject name of topic
     * @param group group for subscribe. Default value is [defaultGroup]
     */
    suspend fun subscribe(
        subject: String,
        group: String? = defaultGroup,
    ): AsyncCloseable {
        checkClosed()
        val subscribeId = Random.nextUuid()
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

    suspend fun readMessage(): NatsMessage {
        return checkConnection().readMessage()
    }

    private var connecting = false
    private var connectionWaters = ArrayList<Continuation<NatsRawConnection>>()

    private suspend fun checkConnection(): NatsRawConnection {
        if (connection != null) {
            return connection!!
        }
        if (connecting) {
            return suspendCoroutine { connectionWaters.add(it) }
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
                    networkDispatcher.tcpConnect(addr)
                } catch (e: SocketConnectException) {
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

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteArray?) {
        checkConnection().publish(
                subject = subject,
                replyTo = replyTo,
                data = data
        )
    }

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteBuffer?) {
        checkConnection().publish(
                subject = subject,
                replyTo = replyTo,
                data = data
        )
    }
}