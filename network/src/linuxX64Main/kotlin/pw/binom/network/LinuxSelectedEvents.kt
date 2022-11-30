package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*

class LinuxSelectedEvents(override val maxElements: Int) : AbstractNativeSelectedEvents() {
    override var selector: LinuxSelector? = null
    override val native = nativeHeap.allocArray<epoll_event>(1000)
    override var eventCount: Int = 0

    override fun close() {
        nativeHeap.free(native)
    }

    override val nativeSelectedKeys: Iterator<AbstractSelector.NativeKeyEvent>
        get() = nativeSelectedKeys2

    override fun resetIterator() {
        nativeSelectedKeys2.reset()
    }

    override fun internalResetFlags() {
        val selector = selector
        repeat(eventCount) { index ->
            val item = native[index]
            if (item.data.u64 == 77uL || item.data.fd == selector?.pipeRead) {
                selector?.interruptWakeup()
                return@repeat
            }
            val ptr = item.data.ptr ?: return@repeat
            val key = ptr.asStableRef<LinuxKey>().get()
            key.internalResetFlags()
        }
    }

    private val nativeSelectedKeys2 = object : Iterator<AbstractSelector.NativeKeyEvent> {
        private val event = object : AbstractSelector.NativeKeyEvent {
            override lateinit var key: AbstractKey
            override var mode: Int = 0
        }
        private var currentNum = 0
        fun reset() {
            currentNum = 0
        }

        private fun check() {
            while (currentNum < eventCount) {
                val selector = selector
                if (native[currentNum].data.fd == selector?.pipeRead) {
                    currentNum++
                    continue
                }
                if (native[currentNum].data.ptr == null) {
                    currentNum++
                    continue
                }
                break
            }
        }

        override fun hasNext(): Boolean {
            check()
            return currentNum < eventCount
        }

        override fun next(): AbstractSelector.NativeKeyEvent {
            if (!hasNext()) {
                throw NoSuchElementException()
            }

            val item = native[currentNum++]

//            val keyPtr = item.data.ptr!!.asStableRef<LinuxKey>()
//            val key = keyPtr.get()
            val key = item.data.ptr!!.asStableRef<LinuxKey>().get()
                ?: error("Key not found ${item.data.u32.toInt()} in ${selector?.native}")
            if (!key.connected) {
                if (/*EPOLLHUP in item.events ||*/ EPOLLERR in item.events) {
                    key.resetMode(0)
                    event.key = key
                    event.mode = Selector.EVENT_ERROR
                    return event
                }
                if (EPOLLOUT in item.events) {
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                    key.resetMode(key.listensFlag)
                    return event
                }
                if (EPOLLIN in item.events) {
                    key.connected = true
                    event.key = key
                    event.mode = Selector.EVENT_CONNECTED or Selector.INPUT_READY
                    key.resetMode(key.listensFlag)
                    return event
                }
                if (EPOLLHUP in item.events) {
//                    key.connected = true
                    event.key = key
                    event.mode = 0
                    return event
                }
                error("Unknown connection state: ${modeToString(item.events.toInt())}")
            }
            if (EPOLLHUP in item.events) {
                event.key = key
                event.mode = Selector.INPUT_READY
                return event
            }
            val common = epollNativeToCommon(item.events.convert())
            if (common == 0) {
                error("Invalid epoll mode: [${modeToString(item.events.convert())}]")
            }
            event.key = key
            event.mode = epollNativeToCommon(item.events.convert())
            if (item.events.toInt() != 0 && event.mode == 0) {
                println("Can't convert ${modeToString(item.events.convert())}")
            }
            return event
        }
    }
}
