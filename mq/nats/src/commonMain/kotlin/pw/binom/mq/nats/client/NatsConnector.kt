package pw.binom.mq.nats.client

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.nextUuid
import kotlin.random.Random

interface NatsConnector : AsyncCloseable {
    companion object {
        /**
         * Creates new nats connection
         */
        fun create(
            clientName: String? = null,
            lang: String = "kotlin",
            echo: Boolean = true,
            tlsRequired: Boolean = false,
            user: String? = null,
            pass: String? = null,
            defaultGroup: String? = null,
            attemptCount: Int = 3,
            networkDispatcher: NetworkManager = Dispatchers.Network,
            serverList: List<NetworkAddress>
        ): NatsConnector =
            NatsConnectorImpl(
                clientName = clientName,
                lang = lang,
                echo = echo,
                tlsRequired = tlsRequired,
                user = user,
                pass = pass,
                defaultGroup = defaultGroup,
                attemptCount = attemptCount,
                networkDispatcher = networkDispatcher,
                serverList = serverList,
            )
    }

    suspend fun subscribe(
        subject: String,
        group: String?,
        subscribeId: String = Random.nextUuid().toString()
    ): AsyncCloseable

    suspend fun readMessage(): NatsMessage
    suspend fun publish(subject: String, replyTo: String? = null, data: ByteArray?)
    suspend fun publish(subject: String, replyTo: String? = null, data: ByteBuffer?)
}
