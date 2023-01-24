package pw.binom.io.socket

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import platform.common.*
import platform.posix.*
import pw.binom.io.IOException
import platform.common.Event as NativeEvent
import platform.common.Selector as NativeSelector

value class Epoll(val raw: CPointer<NativeSelector>) {
    companion object {
        fun create(size: Int): Epoll {
            val descriptor = createSelector(size)
            if (descriptor == null) {
                throw IOException("Can't create epoll. Errno: $errno")
            }
            return Epoll(descriptor)
        }
    }

    enum class EpollResult {
        OK, INVALID, ALREADY_EXIST,
    }

    fun select(events: CPointer<SelectedList>, timeout: Int): Int {
        val count = selectKeys(raw, events, timeout)
        if (count < 0) {
            throw IOException("Error on epoll_wait. errno: $errno")
        }
        return count
    }

    fun add(socket: RawSocket, data: CValuesRef<NativeEvent>?): EpollResult {
//        val ret = epoll_ctl(raw, sEPOLL_CTL_ADD, socket.convert(), data)
        val ret = registryKey(raw, socket, data)
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
//        val ret = epoll_ctl(raw, sEPOLL_CTL_DEL, socket.convert(), null)
        val ret = removeKey(raw, socket)
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

    fun update(socket: RawSocket, data: CValuesRef<NativeEvent>?): Boolean {
        // ENOENT - socket closed
//        val ret = epoll_ctl(raw, sEPOLL_CTL_MOD, socket.convert(), data)
        val ret = updateKey(raw, socket, data)
//        if (ret != 0) {
//            println("Epoll update error: $errno")
//        }
        return ret == 0
    }

    fun close() {
        closeSelector(raw)
//        platform.posix.close(raw)
    }
}
/*
internal fun epollModeToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and sEPOLLIN != 0) {
        sb.append("EPOLLIN ")
    }
    if (mode and sEPOLLPRI != 0) {
        sb.append("EPOLLPRI ")
    }
    if (mode and sEPOLLOUT != 0) {
        sb.append("EPOLLOUT ")
    }
    if (mode and sEPOLLERR != 0) {
        sb.append("EPOLLERR ")
    }
    if (mode and sEPOLLHUP != 0) {
        sb.append("EPOLLHUP ")
    }
    if (mode and sEPOLLRDNORM != 0) {
        sb.append("EPOLLRDNORM ")
    }
    if (mode and sEPOLLRDBAND != 0) {
        sb.append("EPOLLRDBAND ")
    }
    if (mode and sEPOLLWRNORM != 0) {
        sb.append("EPOLLWRNORM ")
    }
    if (mode and sEPOLLWRBAND != 0) {
        sb.append("EPOLLWRBAND ")
    }
    if (mode and sEPOLLMSG != 0) {
        sb.append("EPOLLMSG ")
    }
    if (mode and sEPOLLRDHUP != 0) {
        sb.append("EPOLLRDHUP ")
    }
    if (mode and sEPOLLONESHOT != 0) {
        sb.append("EPOLLONESHOT ")
    }
    return sb.toString().trim()
}
*/
