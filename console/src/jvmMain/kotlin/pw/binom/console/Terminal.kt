package pw.binom.console

import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp

actual object Terminal {
    private val terminal = TerminalBuilder.terminal()
    actual fun getSize(dest: ConsoleSize): Boolean {
        dest.width = terminal.width
        dest.height = terminal.height
        return true
    }

    actual fun setCursorPosition(column: Int, row: Int): Boolean =
        terminal.puts(InfoCmp.Capability.cursor_address, row, column)

    actual fun getCursorPosition(dest: ConsoleSize): Boolean {
        val cursor = terminal.getCursorPosition(null) ?: return false
        dest.width = cursor.x
        dest.height = cursor.y
        return true
    }

    actual fun clear(): Boolean {
        if (!terminal.puts(InfoCmp.Capability.clear_screen)) {
            return false
        }
        terminal.flush()
        return true
    }

    actual fun readChar() = terminal.reader().read()
    actual var echo: Boolean
        get() = terminal.echo()
        set(value) {
            terminal.echo(value)
        }
}
