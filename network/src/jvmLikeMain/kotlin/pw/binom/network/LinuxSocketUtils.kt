package pw.binom.network

internal expect fun TcpClientSocketChannel.internalConnectToUnixSocket(fileName: String)
internal expect fun TcpServerSocketChannel.bindUnixSocket(fileName: String)
