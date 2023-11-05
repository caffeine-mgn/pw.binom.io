package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import platform.common.FLAG_READ
import platform.common.setEventDataPtr
import platform.common.setEventFlags
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.IOException

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class EpollInterceptor actual constructor(selector: Selector) : Closeable {
  private val pipeRead: Int
  private val pipeWrite: Int
  private val wakeupFlag = AtomicBoolean(false)
  private val epoll = selector.epoll

  init {
    val fds = createPipe()
    pipeRead = fds.first
    pipeWrite = fds.second

    val r = selector.usingEventPtr { eventMem ->
      setEventDataPtr(eventMem, null)
      setEventFlags(eventMem, FLAG_READ, 0)
      epoll.add(pipeRead, eventMem)
    }
    if (r != Epoll.EpollResult.OK) {
      platform.posix.close(pipeRead)
      platform.posix.close(pipeWrite)
      throw IOException("Can't init epoll. Can't add default pipe. Status: $r")
    }
  }

  actual fun wakeup() {
    if (wakeupFlag.compareAndSet(false, true)) {
      platform.posix.write(pipeWrite, STUB_BYTE.addressOf(0), 1.convert()).convert<Int>()
    }
  }

  actual fun interruptWakeup() {
    platform.posix.read(pipeRead, STUB_BYTE.addressOf(0), 1.convert())
    wakeupFlag.setValue(false)
  }

  override fun close() {
    epoll.delete(pipeRead)
    platform.posix.close(pipeRead)
    platform.posix.close(pipeWrite)
  }
}
