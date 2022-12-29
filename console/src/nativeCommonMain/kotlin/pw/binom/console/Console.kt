package pw.binom.console

import kotlinx.cinterop.*
import platform.common.internal_terminal_clear_screen
import platform.common.internal_terminal_get_cursor_position
import platform.common.internal_terminal_get_size
import platform.common.internal_terminal_set_cursor_position

actual class Console {
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
}
