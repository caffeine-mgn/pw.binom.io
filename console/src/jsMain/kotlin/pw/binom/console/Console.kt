package pw.binom.console

actual class Console {
    actual fun getSize(dest: ConsoleSize): Boolean = false
    actual fun setCursorPosition(column: Int, row: Int): Boolean = false
    actual fun getCursorPosition(dest: ConsoleSize): Boolean = false
}
