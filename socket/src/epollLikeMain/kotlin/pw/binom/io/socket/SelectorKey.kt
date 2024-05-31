package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import platform.common.*
import platform.socket.*
import platform.posix.errno
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable

@OptIn(ExperimentalForeignApi::class)
actual class SelectorKey(actual val selector: Selector, val socket: Socket) :
  AbstractNativeKey(), Closeable {
  val rawSocket: RawSocket
    get() = socket.native
  actual var attachment: Any? = null
  private var closed = AtomicBoolean(false)
  private var free = AtomicBoolean(false)
  internal val eventMem = NEvent_malloc()!!
//    internal val event = mallocEvent() ?: TODO("Can't allocate event")

  //    @OptIn(ExperimentalTime::class)
//    var lastActiveTime: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
  actual val readFlags
    get() = internalReadFlags
  internal var internalReadFlags = ListenFlags.ZERO
    set(value) {
      field = value
    }
  internal var serverFlag = false

  internal var internalListenFlags = ListenFlags.ZERO

  init {
    NEvent_setEventDataFd(eventMem, socket.native)
    NEvent_setEventFlags(eventMem, 0, 0)
    NetworkMetrics.incSelectorKey()
    NetworkMetrics.incSelectorKeyAlloc()
  }

  private fun resetListenFlags(commonFlags: ListenFlags): Boolean {
    if (closed.getValue() || free.getValue()) {
      return false
    }

    NEvent_setEventDataFd(eventMem, rawSocket)
    NEvent_setEventFlags(eventMem, commonFlags.raw, if (serverFlag) 1 else 0)
    val success = selector.updateKey(this, eventMem)
    if (!success) {
//      val socketError = internal_get_socket_error(rawSocket)
//      println("Can't update epoll flags. rawSocket=$rawSocket error #${internal_get_last_error()}, errno #$errno, socketError #$socketError")
    }
    return success
  }

  actual fun updateListenFlags(listenFlags: ListenFlags): Boolean {
    val updateResult = resetListenFlags(listenFlags)
    return if (updateResult) {
      internalListenFlags = listenFlags
      true
    } else {
      false
    }
  }

  actual val listenFlags: ListenFlags
    get() = internalListenFlags

//    internal val event = nativeHeap.alloc<epoll_event>()

  init {
//        event.data.ptr = self.asCPointer()
//        selector.eventMem.data.fd = rawSocket
//        selector.eventMem.events = 0.convert()
  }

  internal fun internalClose() {
    if (!free.compareAndSet(false, true)) {
      return
    }
    NEvent_free(eventMem)
    NetworkMetrics.decSelectorKeyAlloc()
//        nativeHeap.free(event)
//        freeEvent(event)
    freeSelfClose()
  }

  override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
//        val stack = Throwable().getStackTrace()
//            .map {
//                it.replace('\t', ' ')
//                    .split(' ')
//                    .map { it.trim() }.filter { it.isNotBlank() }
//                    .joinToString(" ")
//            }
//            .joinToString("<-")
//        println("SelectorKey::close attachment: $attachment, stack: $stack")

    NetworkMetrics.decSelectorKey()
    selector.removeKey(this)
  }

  override fun toString(): String = buildToString()

  actual val isClosed: Boolean
    get() = closed.getValue()
}
