package pw.binom.console

import kotlinx.cinterop.*
import platform.common.*

@OptIn(ExperimentalForeignApi::class)
actual object Terminal {
  actual fun getSize(dest: ConsoleSize): Boolean = memScoped {
    val width = alloc<IntVar>()
    val height = alloc<IntVar>()
    val success = internal_terminal_get_size(width.ptr, height.ptr) != 0
    if (success) {
      dest.width = width.value
      dest.height = height.value
    }
    success
  }

  actual fun setCursorPosition(column: Int, row: Int): Boolean =
    internal_terminal_set_cursor_position(column, row) > 0

  actual fun getCursorPosition(dest: ConsoleSize): Boolean = memScoped {
    val width = alloc<IntVar>()
    val height = alloc<IntVar>()
    val success = internal_terminal_get_cursor_position(width.ptr, height.ptr) != 0
    if (success) {
      dest.width = width.value
      dest.height = height.value
    }
    success
  }

  actual fun clear(): Boolean = internal_terminal_clear_screen() != 0
  actual fun readChar(): Int = internal_get_char()
  actual var echo: Boolean
    get() = internal_terminal_get_echo() > 0
    set(value) {
      internal_terminal_set_echo(if (value) 1 else 0)
    }
  actual val isConsoleExist: Boolean
    get() = internal_is_console() > 0

  actual fun readEvent(): Event = pw.binom.console.readEvent()

  actual var mouseTracking: MouseTracking = MouseTracking.OFF
    set(value) {
      field = value
      pw.binom.console.setMouseTracking(value)
    }
}
