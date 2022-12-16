package pw.binom.network

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.convert
import platform.linux.*
import platform.posix.*
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

    enum class EpollResult {
        OK, INVALID, ALREADY_EXIST,
    }

    fun select(events: CArrayPointer<epoll_event>, maxEvents: Int, timeout: Int): Int {
        val count = epoll_wait(
            raw, events, maxEvents, timeout
        )
        return count
    }

    fun add(socket: RawSocket, data: CValuesRef<epoll_event>?): EpollResult {
        val ret = epoll_ctl(raw, EPOLL_CTL_ADD, socket.convert(), data)
        if (ret != 0) {
            when (errno) {
                EPERM, EBADF, ENOENT -> return EpollResult.INVALID
                EEXIST -> return EpollResult.ALREADY_EXIST
                else -> throw IOException("Can't add key to selector. socket: $socket, list: $raw. errno: $errno")
            }
        }
        return EpollResult.OK
    }

    fun delete(socket: RawSocket, failOnError: Boolean): EpollResult {
        val ret = epoll_ctl(raw, EPOLL_CTL_DEL, socket.convert(), null)
        if (ret != 0) {
            when (errno) {
                EPERM, EBADF, ENOENT -> return EpollResult.INVALID
                EEXIST -> return EpollResult.ALREADY_EXIST
            }
            if (failOnError) {
                throw IOException("Can't remove key from selector. socket: $socket, list: $raw. errno: $errno")
            }
        }
        return EpollResult.OK
    }

    fun update(socket: RawSocket, data: CValuesRef<epoll_event>?, failOnError: Boolean) {
        val ret = epoll_ctl(raw, EPOLL_CTL_MOD, socket.convert(), data)
        if (ret != 0 && failOnError) {
            throw IOException("Can't update key to selector. socket: $socket, list: $raw. errno: $errno")
        }
    }

    fun close() {
        platform.posix.close(raw)
    }
}
