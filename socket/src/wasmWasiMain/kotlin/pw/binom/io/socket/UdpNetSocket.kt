package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import kotlin.time.Duration
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@OptIn(UnsafeWasmMemoryApi::class)
actual class UdpNetSocket actual constructor() : UdpSocket, NetSocket {

  companion object {
    const val IP_V4 = 0
    const val IP_V6 = 1
  }

  init {
    withScopedMemoryAllocator { allocator ->
      val ptr = allocator.allocate(8).address.toInt()
      __wasm_import_createUdpSocket(IP_V6, ptr)
    }

  }

  actual override fun close() {
    TODO("Not yet implemented")
  }

  actual override fun setSoTimeout(duration: Duration) {
    TODO("Not yet implemented")
  }

  actual override var blocking: Boolean
    get() = TODO("Not yet implemented")
    set(value) {}
  override val native: Int
    get() = TODO("Not yet implemented")
  override val server: Boolean
    get() = TODO("Not yet implemented")
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = TODO("Not yet implemented")

  actual override fun setTcpNoDelay(value: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  actual override val port: Int?
    get() = TODO("Not yet implemented")

  actual fun bind(address: InetSocketAddress): BindStatus {
    TODO("Not yet implemented")
  }

  actual fun send(data: ByteBuffer, address: InetSocketAddress): Int {
    TODO("Not yet implemented")
  }

  actual fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int {
    TODO("Not yet implemented")
  }

  actual var ttl: UByte
    get() = TODO("Not yet implemented")
    set(value) {}
}
