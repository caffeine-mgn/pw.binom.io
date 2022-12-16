package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*

class MingwSelectedEvents(override val maxElements: Int) : AbstractNativeSelectedEvents() {
    override var selector: MingwSelector? = null
    override val native = nativeHeap.allocArray<epoll_event>(maxElements)
    override var eventCount: Int = 0

    override fun close() {
        nativeHeap.free(native)
    }

    override fun internalResetFlags() {
        TODO("Not implemented")
//        val selector = selector
//        repeat(eventCount) { index ->
//            val item = native[index]
//            if (item.data.fd == selector?.pipeRead) {
//                selector.interruptWakeup()
//                return@repeat
//            }
//
//            val key = selector!!.idToKey[item.data.u32.convert()]
//            key?.internalResetFlags()
//        }
    }

    override val nativeSelectedKeys: Iterator<AbstractNativeSelector.NativeKeyEvent>
        get() = nativeSelectedKeys2

    override fun resetIterator() {
        nativeSelectedKeys2.reset()
    }

    private val nativeSelectedKeys2 = object : Iterator<AbstractNativeSelector.NativeKeyEvent> {
        private val event = object : AbstractNativeSelector.NativeKeyEvent {
            override lateinit var key: AbstractNativeKey
            override var mode: Int = 0
        }
        private var currentNum = 0
        fun reset() {
            currentNum = 0
        }

        override fun hasNext(): Boolean {
            if (currentNum == eventCount) {
                return false
            }
            return true
        }

        override fun next(): AbstractNativeSelector.NativeKeyEvent {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val item = native[currentNum++]
            val key = selector!!.idToKey[item.data.u32.convert()] ?: throw IllegalStateException("Key not found")
//            val keyPtr = item.data.ptr
//                ?.asStableRef<MingwKey>()
//                ?: throw IllegalStateException("Native key not attached to epoll")
//            val key = keyPtr.get()
            if (!key.connected) {
                when {
                    EPOLLERR in item.events || EPOLLRDHUP in item.events/* || EPOLLHUP in item.events*/ -> {
                        key.resetMode(0)
                        event.key = key
                        event.mode = 0
                        return event
                    }
                    EPOLLOUT in item.events -> {
                        event.key = key
                        event.mode = SelectorOld.EVENT_CONNECTED or SelectorOld.OUTPUT_READY
                        key.connected = true
                        key.resetMode(key.listensFlag)
                        return event
                    }
                    EPOLLIN in item.events -> {
                        event.key = key
                        event.mode = SelectorOld.EVENT_CONNECTED or SelectorOld.INPUT_READY
                        key.connected = true
                        key.resetMode(key.listensFlag)
                        return event
                    }
                    else -> throw IllegalStateException(
                        "Connect error. Unknown selector key status. epoll status: ${modeToString(item.events)}, ${
                        item.events.toString(2)
                        }"
                    )
                }
            } else {
//                if (EPOLLHUP in item.events) {
//                    NSocket(native = item.data.sock, family = AF_INET).close()
//                    key.close()
//                    event.key = key
//                    event.mode = Selector.EVENT_ERROR
//                    return event
//                }
                event.key = key
                event.mode = epollNativeToCommon(item.events.convert())
                if (item.events.toInt() != 0 && event.mode == 0) {
                    throw Exception("MingwSelector: Can't convert ${modeToString(item.events)} to common")
                }
                return event
            }
        }
    }
}
