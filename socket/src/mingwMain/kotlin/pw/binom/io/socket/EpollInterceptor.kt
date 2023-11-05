package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import platform.common.FLAG_READ
import platform.common.setEventDataPtr
import platform.common.setEventFlags
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable

@OptIn(ExperimentalForeignApi::class)
actual class EpollInterceptor actual constructor(selector: Selector) : Closeable {

  private val udpReadSocket = Socket.createUdpNetSocket()
  private val udpWriteSocket = Socket.createUdpNetSocket()
  private val wakeupFlag = AtomicBoolean(false)
  private val addr: InetNetworkAddress
  private val epoll = selector.epoll

  init {
    val b = udpReadSocket.bind(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
    udpReadSocket.blocking = false
    udpWriteSocket.blocking = false
    check(b == BindStatus.OK) { "Can't bind port" }
    val r = selector.usingEventPtr { eventMem ->
      setEventDataPtr(eventMem, null)
      setEventFlags(eventMem, FLAG_READ, 0)
      epoll.add(udpReadSocket.native, eventMem)
    }
    if (r != Epoll.EpollResult.OK) {
      udpReadSocket.close()
      udpWriteSocket.close()
    }
    addr = InetNetworkAddress.create(host = "127.0.0.1", port = udpReadSocket.port!!)
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

  override fun close() {
    epoll.delete(udpReadSocket.native)
    udpReadSocket.close()
    udpWriteSocket.close()
  }
}
