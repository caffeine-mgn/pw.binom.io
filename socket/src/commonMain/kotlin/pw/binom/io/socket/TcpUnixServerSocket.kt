package pw.binom.io.socket

interface TcpUnixServerSocket : TcpServerSocket {
  fun accept(address: ((String) -> Unit)?): TcpClientNetSocket?
  fun accept(): TcpClientNetSocket? = accept(null)
  fun bind(path: String): BindStatus
}
