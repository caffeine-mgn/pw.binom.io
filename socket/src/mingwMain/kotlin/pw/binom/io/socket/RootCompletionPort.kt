package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.internal_socket_to_HANDLE
import platform.windows.*
import pw.binom.io.Closeable
import kotlin.time.Duration

class RootCompletionPort : Closeable {
  enum class WaitResult {
    ERROR,
    TIMEOUT,
    OK,
  }

  private val native = CreateIoCompletionPort(INVALID_HANDLE_VALUE, null, 0.convert(), 1.convert())
    ?: throw RuntimeException("Can't create CompletionPort")

  fun add(handle: HANDLE, completionKey: ULONG_PTR = 0.convert()): CompletionPort {
    val newport = CreateIoCompletionPort(handle, native, completionKey, 1.convert())
    if (newport == null) {
      val error = GetLastError()
      throw RuntimeException("Can't add handle to CompletionPort. Error: #$error")
    }
    return CompletionPort(newport)
  }

  fun add(socket: RawSocket, completionKey: ULONG_PTR = 0.convert()) = add(
    handle = internal_socket_to_HANDLE(socket.convert())!!,
    completionKey = completionKey,
  )

  fun waitEvent(dest: Dest, duration: Duration = Duration.INFINITE): WaitResult {
    val timeout = if (duration.isInfinite()) INFINITE else duration.inWholeMilliseconds.toUInt()
    dest.lpOverlapped.value = null
    dest.dwTransferred.value = 0u
    dest.ulCompletionKey.value = 0u
    val result = GetQueuedCompletionStatus(
      native,
      dest.dwTransferred.ptr,
      dest.ulCompletionKey.ptr,
      dest.lpOverlapped.ptr,
      timeout,
    )
    println("GetQueuedCompletionStatus result=$result")
    if (result == 0) {
      val lastError = GetLastError()
      if (lastError == WAIT_TIMEOUT.toUInt()) {
        return WaitResult.TIMEOUT
      }
      return WaitResult.ERROR
    } else {
      return WaitResult.OK
    }
  }

  override fun close() {
    CloseHandle(native)
  }

  class Dest : Closeable {
    val dwTransferred = nativeHeap.alloc<UIntVar>()
    val ulCompletionKey = nativeHeap.alloc<ULongVar>()
    val lpOverlapped = nativeHeap.alloc<LPOVERLAPPEDVar>()

    override fun close() {
      nativeHeap.free(dwTransferred)
      nativeHeap.free(ulCompletionKey)
      nativeHeap.free(lpOverlapped)
    }
  }
}
