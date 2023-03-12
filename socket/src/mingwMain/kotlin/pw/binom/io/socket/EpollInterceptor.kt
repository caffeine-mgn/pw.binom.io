package pw.binom.io.socket

import platform.common.FLAG_READ
import platform.common.setEventDataPtr
import platform.common.setEventFlags
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

actual class EpollInterceptor actual constructor(selector: Selector) : Closeable {

    private val udpReadSocket = Socket.createUdpNetSocket()
    private val udpWriteSocket = Socket.createUdpNetSocket()
    private val wakeupFlag = AtomicBoolean(false)
    private val addr: NetworkAddress
    private val stubByte = ByteBuffer(1)
    private val epoll = selector.epoll

    init {
        val b = udpReadSocket.bind(NetworkAddress.create(host = "127.0.0.1", port = 0))
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
        addr = NetworkAddress.create(host = "127.0.0.1", port = udpReadSocket.port!!)
    }

    actual fun wakeup() {
        if (wakeupFlag.compareAndSet(false, true)) {
            stubByte.clear()
            udpWriteSocket.send(stubByte, addr)
        }
    }

    actual fun interruptWakeup() {
        stubByte.clear()
        udpReadSocket.receive(stubByte, null)
        wakeupFlag.setValue(false)
    }

    override fun close() {
        stubByte.close()
        epoll.delete(udpReadSocket.native)
        udpReadSocket.close()
        udpWriteSocket.close()
    }
}
