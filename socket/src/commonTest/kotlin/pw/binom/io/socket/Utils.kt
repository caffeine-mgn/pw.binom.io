package pw.binom.io.socket

import kotlin.test.assertEquals

const val HTTP_SERVER_PORT = 7143
val httpServerAddress = NetworkAddress.create("127.0.0.1", HTTP_SERVER_PORT)

fun TcpNetServerSocket.bind(): Int {
    bind(NetworkAddress.create(host = "127.0.0.1", port = 0))
    return port!!
}

fun TcpClientNetSocket.connect(server: TcpNetServerSocket) =
    connect(NetworkAddress.create(host = "127.0.0.1", port = server.port!!))

fun ConnectStatus.assertOk(): ConnectStatus {
    assertEquals(ConnectStatus.OK, this)
    return this
}

fun ConnectStatus.assertInProgress(): ConnectStatus {
    assertEquals(ConnectStatus.IN_PROGRESS, this)
    return this
}
