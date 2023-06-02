package pw.binom.console

actual object Terminal {
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

    actual fun readChar(): Int = 0
    actual var echo: Boolean
        get() = false
        set(value) {}
}
