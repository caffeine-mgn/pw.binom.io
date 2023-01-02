package pw.binom.console

actual class Console {
    actual fun getSize(dest: ConsoleSize): Boolean = false
    actual fun setCursorPosition(column: Int, row: Int): Boolean = false
    actual fun getCursorPosition(dest: ConsoleSize): Boolean = false
    actual fun clear(): Boolean {
        val consoleFunc = console.asDynamic().clear.unsafeCast<(() -> Unit)?>()
        return if (consoleFunc != null) {
            try {
                consoleFunc()
                true
            } catch (e: dynamic) {
                false
            }
        } else {
            false
        }
    }
}
