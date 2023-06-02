package pw.binom.console

expect object Terminal {

    var echo: Boolean

    fun getSize(dest: ConsoleSize): Boolean
    fun setCursorPosition(column: Int, row: Int): Boolean
    fun getCursorPosition(dest: ConsoleSize): Boolean
    fun clear(): Boolean
    fun readChar(): Int
}
