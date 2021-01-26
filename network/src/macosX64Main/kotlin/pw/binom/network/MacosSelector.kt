package pw.binom.network

import kotlinx.cinterop.*
import platform.darwin.*
import platform.linux.EVFILT_EMPTY
import platform.linux.internal_EV_SET
import platform.posix.ENOENT
import platform.posix.errno
import platform.posix.timespec
import pw.binom.io.IOException

class LinuxSelector : AbstractSelector() {

    class MingwKey(val kqueueNative: Int, attachment: Any?, socket: NSocket) : AbstractKey(attachment, socket) {
        override fun isSuccessConnected(nativeMode: Int): Boolean =
            EVFILT_WRITE in nativeMode || EV_EOF !in nativeMode
        //TODO("nativeMode=${nativeMode.toUInt().toString(2)}")
//            EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

        fun epollCommonToNative(mode: Int): Int {
            var events = 0
            if (Selector.INPUT_READY in mode) {
                events = events or EVFILT_READ
            }
            if (!connected && Selector.EVENT_CONNECTED in mode) {
                events = events or EVFILT_WRITE
            }
            if (connected && Selector.OUTPUT_READY in mode) {
                events = events or EVFILT_WRITE
            }
            if (events == 0) {
                return EVFILT_EMPTY
            }
            return events
        }

        override fun resetMode(mode: Int) {
            memScoped {
                val event = alloc<kevent>()
                internal_EV_SET(
                    event.ptr,
                    socket.native,
                    epollCommonToNative(mode),
                    EV_ADD or EV_ENABLE or EV_CLEAR,
                    0,
                    0,
                    ptr
                )
                if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                    throw IOException("Can't reset kevent filter. errno: $errno")
                }
            }
        }

        override fun close() {
            super.close()
            memScoped {
                val event = alloc<kevent>()
                internal_EV_SET(event.ptr, socket.native, EVFILT_EMPTY, EV_ADD or EV_CLEAR, 0, 0, null)
                if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                    if (errno != ENOENT) {
                        throw IOException("Can't reset kevent filter. errno: $errno")
                    }
                }
                internal_EV_SET(event.ptr, socket.native, EVFILT_EMPTY, EV_DELETE or EV_DISABLE or EV_CLEAR, 0, 0, null)
                if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                    if (errno != ENOENT) {
                        throw IOException("Can't reset kevent filter. errno: $errno")
                    }
                }
            }
        }
    }

    private val kqueueNative = kqueue()//epoll_create(1000)!!

    init {
        if (kqueueNative == -1) {
            throw IOException("Can't init kqueue. errno: $errno")
        }
    }

    private val list = nativeHeap.allocArray<kevent>(1000)//nativeHeap.allocArray<epoll_event>(1000)


    override fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey {
        val key = MingwKey(kqueueNative, attachment, socket)
        memScoped {
            val event = alloc<kevent>()
            if (!connectable) {
                key.connected = true
            }


            internal_EV_SET(
                event.ptr,
                socket.native,
                key.epollCommonToNative(mode),
                EV_ADD or EV_ENABLE,
                0,
                0,
                key.ptr
            )
            if (kevent(kqueueNative, event.ptr, 1, null, 0, null) == -1) {
                throw IOException("Can't set filter of kevent mode to 0b${mode.toUInt().toString(2)}, errno: $errno")
            }
            //key.listensFlag = mode
        }
        return key
    }

    override fun nativeSelect(timeout: Long, func: (AbstractKey, mode: Int) -> Unit): Int {
        val eventCount = memScoped {
            val time = if (timeout < 0)
                null
            else {

                val c = alloc<timespec>()
                c.tv_sec = timeout / 1000L
                c.tv_nsec = (timeout - c.tv_sec * 1000) * 1000L
                c.ptr
            }
            kevent(kqueueNative, null, 0, list, 1000, time)
        }
        if (eventCount <= 0) {
            return 0
        }
        var count = 0
        for (i in 0 until eventCount) {
            val item = list[i]
            val keyPtr = item.udata?.asStableRef<MingwKey>()?:continue
            val key = keyPtr.get()
            if (key.closed){
                continue
            }
            count++
            if (EV_EOF in item.flags) {
                key.resetMode(0)
                func(key, Selector.EVENT_ERROR)
            } else {
                if (!key.connected && EVFILT_WRITE in item.filter.toInt()) {
                    key.resetMode(0)
                    func(key, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
                    key.connected = true
                } else {
                    var eventCode = 0
                    if (EVFILT_WRITE in item.filter.toInt()) {
                        eventCode = eventCode or Selector.OUTPUT_READY
                    }
                    if (EVFILT_READ in item.filter.toInt()) {
                        eventCode = eventCode or Selector.INPUT_READY
                    }
                    func(key, eventCode)
                }
            }
        }
        return count
    }

    override fun close() {
        platform.posix.close(kqueueNative)
        nativeHeap.free(list)
    }
}


private fun epollNativeToCommon(mode: Int): Int {
    var events = 0
    if (EVFILT_READ in mode) {
        events = events or Selector.INPUT_READY
    }
    if (EVFILT_WRITE in mode) {
        events = events or Selector.OUTPUT_READY
    }
    return events
}

actual fun createSelector(): Selector = LinuxSelector()

fun modeToString1(mode: Int): String {
    val sb = StringBuilder()
    if (mode and EVFILT_READ != 0)
        sb.append("EVFILT_READ ")
    if (mode and EVFILT_WRITE != 0)
        sb.append("EVFILT_WRITE ")

//    if (mode and EVFILT_EMPTY != 0)
//        sb.append("EVFILT_EMPTY ")

    if (mode and EVFILT_AIO != 0)
        sb.append("EVFILT_AIO ")

    if (mode and EVFILT_VNODE != 0)
        sb.append("EVFILT_VNODE ")

    if (mode and EVFILT_PROC != 0)
        sb.append("EVFILT_PROC ")

//    if (mode and EVFILT_PROCDESC != 0)
//        sb.append("EVFILT_PROCDESC ")

    if (mode and EVFILT_SIGNAL != 0)
        sb.append("EVFILT_SIGNAL ")
    if (mode and EVFILT_TIMER != 0)
        sb.append("EVFILT_TIMER ")
    if (mode and EVFILT_USER != 0)
        sb.append("EVFILT_USER ")

    return sb.toString()
}

fun flagsToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and EV_ADD != 0)
        sb.append("EV_ADD ")
    if (mode and EV_ENABLE != 0)
        sb.append("EV_ENABLE ")

    if (mode and EV_DISABLE != 0)
        sb.append("EV_DISABLE ")

    if (mode and EV_DISPATCH != 0)
        sb.append("EV_DISPATCH ")

    if (mode and EV_DISPATCH2 != 0)
        sb.append("EV_DISPATCH2 ")

    if (mode and EV_DELETE != 0)
        sb.append("EV_DELETE ")

    if (mode and EV_RECEIPT != 0)
        sb.append("EV_RECEIPT ")

    if (mode and EV_ONESHOT != 0)
        sb.append("EV_ONESHOT ")

    if (mode and EV_CLEAR != 0)
        sb.append("EV_CLEAR ")

    if (mode and EV_EOF != 0)
        sb.append("EV_EOF ")

    if (mode and EV_ERROR != 0)
        sb.append("EV_ERROR ")

    return sb.toString()
}