package pw.binom.io.socket

expect class TcpClientUnixSocket() : TcpClientSocket {
  fun connect(path: String): ConnectStatus
}
