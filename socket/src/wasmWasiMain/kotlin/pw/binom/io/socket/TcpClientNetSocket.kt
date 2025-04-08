package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.wasm.wasi.*
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@OptIn(UnsafeWasmMemoryApi::class)
actual open class TcpClientNetSocket actual constructor() : TcpClientSocket, NetSocket {

  /**
   * https://github.com/WebAssembly/wasi-libc/blob/e9524a0980b9bb6bb92e87a41ed1055bdda5bb86/libc-bottom-half/sources/wasip2.c#L4160
   */
  override val native: Int =
    withScopedMemoryAllocator { allocator ->
      val retPtr = allocator.allocate(tcp_create_socket_error_code_t.SIZE_IN_BYTES)
      __wasm_import_tcp_create_socket_create_tcp_socket(
        address_family = NETWORK_IP_ADDRESS_FAMILY_IPV6,
        ptr = retPtr.address
      )
      tcp_create_socket_error_code_t(retPtr).let {
        if (it.error != 0) {
          throw IllegalStateException("Can't create TCP socket")
        }
        it.socket
      }
    }


  actual override fun close() {
    __imported_wasi_snapshot_preview1_sock_shutdown(
      fd = native,
      how = TCP_SHUTDOWN_TYPE_BOTH
    )
  }

  actual override fun send(data: ByteBuffer): Int {
    withScopedMemoryAllocator { allocator ->
      val dataLen = data.remaining
      val dataPtr = allocator.allocate(dataLen)
      val retPtr = allocator.allocate(4)
      var p = dataPtr
      while (data.hasRemaining) {
        p.storeByte(data.getByte())
        p += 1
      }
      __imported_wasi_snapshot_preview1_sock_send(
        fd = native,
        si_data = dataPtr.address,
        si_data_len = dataLen,
        si_flags = 0,
        retptr0 = retPtr.address
      )
    }
    TODO("Not yet implemented")
  }

  actual override fun receive(data: ByteBuffer): Int {
    withScopedMemoryAllocator { allocator ->
      val dataPtr = allocator.allocate(data.remaining)
      val retptr0 = allocator.allocate(4)
      val retptr1 = allocator.allocate(4)
      __imported_wasi_snapshot_preview1_sock_recv(
        fd = native,
        ri_data = dataPtr.address,
        ri_data_len = data.remaining,
        ri_flags = 0,
        retptr0 = retptr0.address,
        retptr1 = retptr1.address,
      )
    }
    TODO("Not yet implemented")
  }

  actual override var blocking: Boolean
    get() = TODO("Not yet implemented")
    set(value) {}
  actual override val tcpNoDelay: Boolean
    get() = TODO("Not yet implemented")
  actual override val id: String
    get() = TODO("Not yet implemented")

  actual override fun setTcpNoDelay(value: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  actual override val port: Int?
    get() = TODO("Not yet implemented")
  override val server: Boolean
    get() = false
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}

  actual fun connect(address: InetSocketAddress): ConnectStatus {
    TODO("Not yet implemented")
  }
}
