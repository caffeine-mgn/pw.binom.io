package pw.binom.network

internal actual fun TcpClientSocketChannel.internalConnectToUnixSocket(fileName: String) {
    throwUnixSocketNotSupported()
}

internal actual fun TcpServerSocketChannel.bindUnixSocket(fileName: String) {
    throwUnixSocketNotSupported()
}

internal fun throwUnixSocketNotSupported(): Nothing =
    throw RuntimeException("Mingw Target not supports Unix Domain Socket")
