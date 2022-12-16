package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.*

value class HeapMemory<T : CVariable>(val data: ByteArray) {
    companion object {
        fun <T : CVariable> create(size: Int) = HeapMemory<T>(ByteArray(size))
        inline fun <reified T : CVariable> create() = create<T>(sizeOf<T>().convert())
    }

    inline fun <R> ptr(offset: Int, func: (CPointer<T>) -> R): R = data.usePinned {
        func(it.addressOf(offset).reinterpret())
    }

    inline fun <R> ptr(func: (CPointer<T>) -> R) = ptr(offset = 0, func = func)
}

value class HeapValue<T : CVariable>(val heap: ByteArray) {
    companion object {
        internal inline fun <reified T : CVariable> alloc(): HeapValue<T> =
            HeapValue(ByteArray(sizeOf<T>().convert()))

        internal inline fun <reified T : CVariable> alloc(size: Int): HeapValue<T> =
            HeapValue(ByteArray(sizeOf<T>().convert<Int>() * size))
    }

    inline fun <R> content(func: (CPointer<T>) -> R) = content(offset = 0, func = func)

    inline fun <R> content(offset: Int, func: (CPointer<T>) -> R) = heap.usePinned {
        func(it.addressOf(offset).reinterpret())
    }
}

// private val counter = AtomicInt(0)
// private val allKeys = defaultMutableSet<LinuxKey>()
// private val keysLock = SpinLock()

class LinuxKey(
    val list: Epoll,
    attachment: Any?,
    override val selector: LinuxSelector,
) : AbstractNativeKey(attachment = attachment) {
    //    internal val epollEvent = HeapValue.alloc<epoll_event>()
    internal var epollEvent: epoll_event? = null

    //    internal val epollEvent = nativeHeap.alloc<epoll_event>()
    internal var nativeSocket: RawSocket = 0
    private var selfPtr: StableRef<LinuxKey>? = null

    override fun setRaw(raw: RawSocket) {
        if (nativeSocket == raw) {
            return
        }
        check(nativeSocket == 0) { "Native socket already set" }
        val v = StableRef.create(this)
        NetworkMetrics.incSelectorKeyAlloc()
//        println("NEW KEY. ${counter.addAndGet(1)}   ${NetworkMetrics.selectorKeyAllocCountMetric.value}")
        selfPtr = v
        val epollEvent = nativeHeap.alloc<epoll_event>()
        epollEvent.events = epollCommonToNative(listensFlag.convert()).convert()
        println("LinuxKey: create with mode: ${modeToString(epollCommonToNative(listensFlag))}, attachment: $attachment")
        epollEvent.data.ptr = v.asCPointer()
        this.epollEvent = epollEvent
        nativeSocket = raw

        selector.addKey(this)
//        keysLock.synchronize {
//            allKeys += this
//        }
//
//        keysLock.synchronize {
//            val kk = selector.keys
//            println("---===ACTIVE KEYS===---")
//            kk.forEach {
//                println("->$it")
//            }
//            println("---===ACTIVE KEYS===---")
//            println("---===OTHER KEYS===---")
//            allKeys.forEach {
//                if (it in kk) {
//                    return@forEach
//                }
//                println("->$it")
//            }
//            println("---===OTHER KEYS===---")
//        }
    }

    internal fun internalFree() {
        val epollEvent = epollEvent
        if (epollEvent != null) {
            epollEvent.data.ptr = null
            epollEvent.events = 0.convert()
            list.update(socket = nativeSocket, data = epollEvent.ptr, failOnError = false)
            nativeHeap.free(epollEvent)
            this.epollEvent = null
        }
        selfPtr?.let {
            NetworkMetrics.decSelectorKeyAlloc()
            it.dispose()
//            keysLock.synchronize {
//                allKeys -= this
//            }
//            println("REMOVE KEY. ${counter.addAndGet(-1)} ${NetworkMetrics.selectorKeyAllocCountMetric.value}")
        }
        selfPtr = null
        nativeSocket = 0
    }

//    override fun removeSocket(raw: RawSocket) {
//        if (nativeSocket == raw) {
//            println("Remove ok!!!")
//            selector.removeKey(this, raw)
// //            selector.wakeup()
//            return
//        } else {
//            println("Remove fail!!! raw=$raw nativeSocket=$nativeSocket")
//        }
//    }

    override fun isSuccessConnected(nativeMode: Int): Boolean =
        EPOLLOUT in nativeMode && EPOLLERR !in nativeMode && EPOLLRDHUP !in nativeMode

    override fun resetMode(mode: Int) {
        if (nativeSocket == 0) {
            println("LinuxKey: native socket is 0. attachment: $attachment")
            return
        }
        println("LinuxKey: Change mode to ${modeToString(epollCommonToNative(listensFlag))}, attachment: $attachment")
        val epollEvent = epollEvent
        if (epollEvent != null) {
            epollEvent.events = epollCommonToNative(mode.convert()).convert()
            if (nativeSocket != 0) {
                list.update(socket = nativeSocket, data = epollEvent.ptr, failOnError = false)
            }
        }
    }

    override fun close() {
        if (!internalClose()) {
            return
        }
        val nativeSocket = nativeSocket
        if (nativeSocket != 0) {
//            println("Remove Key")
            selector.removeKey(this)
//            removeSocket(nativeSocket)
//            if (epoll_ctl(list, EPOLL_CTL_DEL, nativeSocket.convert(), epollEvent.ptr) != 0) {
//                throw IOException("Can't remove SelectorKey from Selector")
//            }
        } else {
//            println("Key already removed!")
        }
        selector.undefineKey(this)
    }

    override fun toString(): String =
        "LinuxKey(native=${modeToString(epollCommonToNative(listensFlag))}, connected=$connected, ${generateToString()})"

    fun epollCommonToNative(mode: Int): Int {
        var events = EPOLLONESHOT
        if (SelectorOld.EVENT_ERROR in mode) {
            events = events or EPOLLHUP or EPOLLERR
        }
        if (SelectorOld.INPUT_READY in mode) {
            events = events or EPOLLIN or EPOLLHUP or EPOLLERR
        }
        if (!connected && SelectorOld.EVENT_CONNECTED in mode) {
            events = events or EPOLLOUT
        }
        if (connected && SelectorOld.OUTPUT_READY in mode) {
            events = events or EPOLLOUT
        }
        return events
    }
}
