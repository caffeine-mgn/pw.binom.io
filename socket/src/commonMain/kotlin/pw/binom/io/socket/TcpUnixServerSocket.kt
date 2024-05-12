package pw.binom.io.socket

expect class TcpUnixServerSocket() : TcpServerSocket {
  fun accept(): TcpClientUnixSocket?
  fun bind(path: String): BindStatus
}
