package pw.binom.io.socket

import platform.posix.*

private var socketInited = false

internal fun initNativeSocket(): SOCKET {
    if (!socketInited)
        init_sockets()
    socketInited = true
    return socket(AF_INET, SOCK_STREAM, 0)
}