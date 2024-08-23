package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import platform.socket.FLAG_READ
import platform.socket.NEvent_setEventDataPtr
import platform.socket.NEvent_setEventFlags
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class EpollInterceptor actual constructor(selector: Selector) : Closeable {

  private val udpReadSocket = UdpNetSocket()
  private val udpWriteSocket = UdpNetSocket()
  private val wakeupFlag = AtomicBoolean(false)
  private val addr: InetSocketAddress
  private val epoll = selector.epoll

  init {
    val b = udpReadSocket.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
    udpReadSocket.blocking = false
    udpWriteSocket.blocking = false
    check(b == BindStatus.OK) { "Can't bind port" }
    val r = selector.usingEventPtr { eventMem ->
      NEvent_setEventDataPtr(eventMem, null)
      NEvent_setEventFlags(eventMem, FLAG_READ, 0)
      epoll.add(udpReadSocket.native, eventMem)
    }
    if (r != Epoll.EpollResult.OK) {
      udpReadSocket.close()
      udpWriteSocket.close()
    }
    addr = InetSocketAddress.resolve(host = "127.0.0.1", port = udpReadSocket.port!!)
  }

  actual fun wakeup() {
    if (wakeupFlag.compareAndSet(false, true)) {
      platform.posix.write(udpWriteSocket.native, STUB_BYTE.addressOf(0), 1.convert()).convert<Int>()
    }
  }

  actual fun interruptWakeup() {
    platform.posix.read(udpReadSocket.native, STUB_BYTE.addressOf(0), 1.convert())
    wakeupFlag.setValue(false)
  }

  actual override fun close() {
    epoll.delete(udpReadSocket.native)
    udpReadSocket.close()
    udpWriteSocket.close()
  }
}
