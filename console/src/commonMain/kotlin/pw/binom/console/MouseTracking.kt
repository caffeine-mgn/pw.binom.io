package pw.binom.console

enum class MouseTracking {
  /**
   * Disable mouse tracking
   */
  OFF,

  /**
   * Track button press and release.
   */
  NORMAL,

  /**
   * Also report button-motion events. Mouse movements are reported if the mouse pointer has moved to a different character cell.
   */
  BUTTON,

  /**
   * Report all motions events, even if no mouse button is down.
   */
  ANY,
}
