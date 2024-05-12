package pw.binom.mq.nats.client

import pw.binom.io.socket.SocketAddress

data class ConnectInfo(
    val serverId: String,
    val serverName: String,
    val maxPayload: Long,
    val clientId: Int,
    val proto: Int,
    val jetStreamEnabled: Boolean,
    val authRequired: Boolean,
    val headersSupported: Boolean,
    val clusterAddresses: List<SocketAddress>,
)
