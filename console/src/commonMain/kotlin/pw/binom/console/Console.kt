package pw.binom.console

expect class Console {
    fun getSize(dest: ConsoleSize): Boolean
    fun setCursorPosition(column: Int, row: Int): Boolean
    fun getCursorPosition(dest: ConsoleSize): Boolean
    fun clear(): Boolean
}
