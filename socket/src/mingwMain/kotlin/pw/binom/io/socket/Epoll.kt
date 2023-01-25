package pw.binom.io.socket
/*
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.convert
import platform.common.*
import platform.posix.*
import pw.binom.io.IOException

value class Epoll(val raw: HANDLE) {
    companion object {
        fun create(size: Int): Epoll {
            val descriptor = epoll_create(size)
            if (descriptor == null) {
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
        if (count < 0) {
            throw IOException("Error on epoll_wait. errno: $errno")
        }
        return count
    }

    fun add(socket: RawSocket, data: CValuesRef<epoll_event>?): EpollResult {
        val ret = epoll_ctl(raw, EPOLL_CTL_ADD, socket.convert(), data)
//        if (ret == 0) {
//            if (data != null) {
//                data as CPointer<epoll_event>
//                val events = data[0].events
//                println("======>Epoll $raw: add socket $socket. mode: ${modeToString(events.convert())}")
//            }
//        } else {
//            println("======>Epoll $raw: error add socket $socket")
//        }
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
        epoll_close(raw)
    }
}

internal fun epollModeToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and EPOLLIN.toInt() != 0) {
        sb.append("EPOLLIN ")
    }
    if (mode and EPOLLPRI.toInt() != 0) {
        sb.append("EPOLLPRI ")
    }
    if (mode and EPOLLOUT.toInt() != 0) {
        sb.append("EPOLLOUT ")
    }
    if (mode and EPOLLERR.toInt() != 0) {
        sb.append("EPOLLERR ")
    }
    if (mode and EPOLLHUP.toInt() != 0) {
        sb.append("EPOLLHUP ")
    }
    if (mode and EPOLLRDNORM.toInt() != 0) {
        sb.append("EPOLLRDNORM ")
    }
    if (mode and EPOLLRDBAND.toInt() != 0) {
        sb.append("EPOLLRDBAND ")
    }
    if (mode and EPOLLWRNORM.toInt() != 0) {
        sb.append("EPOLLWRNORM ")
    }
    if (mode and EPOLLWRBAND.toInt() != 0) {
        sb.append("EPOLLWRBAND ")
    }
    if (mode and EPOLLMSG.toInt() != 0) {
        sb.append("EPOLLMSG ")
    }
    if (mode and EPOLLRDHUP.toInt() != 0) {
        sb.append("EPOLLRDHUP ")
    }
    if (mode and EPOLLONESHOT.toInt() != 0) {
        sb.append("EPOLLONESHOT ")
    }
    return sb.toString().trim()
}
*/
