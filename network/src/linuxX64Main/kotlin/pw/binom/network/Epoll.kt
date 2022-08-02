package pw.binom.network

import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.convert
import platform.linux.*
import platform.posix.errno
import pw.binom.io.IOException

value class Epoll(val raw: Int) {
    companion object {
        fun create(size: Int): Epoll {
            val descriptor = epoll_create(size)
            if (descriptor == -1) {
                throw IOException("Can't create epoll. Errno: $errno")
            }
            return Epoll(descriptor)
        }
    }

    fun add(socket: RawSocket, data: CValuesRef<epoll_event>?) {
        if (epoll_ctl(raw, EPOLL_CTL_ADD, socket.convert(), data) != 0) {
            throw IOException("Can't add key to selector. socket: $socket, list: $raw. errno: $errno")
        }
    }

    fun delete(socket: RawSocket, failOnError: Boolean) {
        if (epoll_ctl(raw, EPOLL_CTL_DEL, socket.convert(), null) != 0 && failOnError) {
            throw IOException("Can't remove key from selector. socket: $socket, list: $raw. errno: $errno")
        }
    }

    fun update(socket: RawSocket, data: CValuesRef<epoll_event>?, failOnError: Boolean) {
        if (epoll_ctl(raw, EPOLL_CTL_MOD, socket.convert(), data) != 0 && failOnError) {
            throw IOException("Can't update key to selector. socket: $socket, list: $raw. errno: $errno")
        }
    }

    fun close() {
        platform.posix.close(raw)
    }
}
