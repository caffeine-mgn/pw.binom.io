package pw.binom.io.socket

import pw.binom.testing.shouldEquals

const val HTTP_SERVER_PORT = 7143
const val UDP_ECHO_PORT = 8143
val httpServerAddress = DomainNetworkAddress("127.0.0.1").resolve()!!.withPort(HTTP_SERVER_PORT)

fun TcpNetServerSocket.bind(): Int {
  bind(DomainNetworkAddress("127.0.0.1").resolve()!!.withPort(0)) shouldEquals BindStatus.OK
  return port!!
}

fun TcpClientNetSocket.connect(server: TcpNetServerSocket) =
  connect(DomainNetworkAddress("127.0.0.1").resolve()!!.withPort(server.port!!))

fun ConnectStatus.assertOk(): ConnectStatus {
  this shouldEquals ConnectStatus.OK
  return this
}

fun ConnectStatus.assertInProgress(): ConnectStatus {
  this shouldEquals ConnectStatus.IN_PROGRESS
  return this
}
