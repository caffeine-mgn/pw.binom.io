package pw.binom.console

expect object Terminal {
  val isConsoleExist: Boolean

  var echo: Boolean
  var mouseTracking: MouseTracking

  fun getSize(dest: ConsoleSize): Boolean
  fun setCursorPosition(column: Int, row: Int): Boolean
  fun getCursorPosition(dest: ConsoleSize): Boolean
  fun clear(): Boolean
  fun readChar(): Int
  fun readEvent(): Event
  fun enterRawMode()
  fun restoreState()
}
