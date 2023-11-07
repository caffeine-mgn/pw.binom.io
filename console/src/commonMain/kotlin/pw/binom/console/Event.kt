package pw.binom.console

sealed interface Event {

  interface Modifier {
    val isShift: Boolean
    val isAlt: Boolean
    val isControl: Boolean
  }

  interface Mouse : Event {
    val x: Int
    val y: Int
    val button: Button
    val modifier: Modifier
  }

  interface Key : Event {
    val char: Char
  }

  enum class Button {
    NoButton,
    Button1,
    Button2,
    Button3,
    WheelUp,
    WheelDown,
  }
}
