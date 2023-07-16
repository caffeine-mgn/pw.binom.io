package pw.binom.io.socket

import platform.windows.CloseHandle
import platform.windows.HANDLE

value class CompletionPort(val handle: HANDLE) {
  fun close() {
    CloseHandle(handle)
  }
}
