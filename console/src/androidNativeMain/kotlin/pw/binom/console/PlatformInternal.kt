package pw.binom.console
/*
actual fun readEvent(): Event {
  val control = Terminal.readChar()
  if (control != 27) {
    KeyEventImpl.char = control.toChar()
    return KeyEventImpl
  }

  val m1 = Terminal.readChar() // 0b1011011 // 91 // [
  val m2 = Terminal.readChar() // 0b1001101 // 77 // M

  val cb = Terminal.readChar()
  val cx = Terminal.readChar() - ' '.code - 1
  val cy = Terminal.readChar() - ' '.code - 1
  ModifierImpl.isShift = (cb and 4) == 4
  ModifierImpl.isAlt = (cb and 8) == 8
  ModifierImpl.isControl = (cb and 16) == 16
  MouseEventImpl.x = cx
  MouseEventImpl.y = cy

  MouseEventImpl.button = if ((cb and 64) == 64) {
    if ((cb and 1) == 1) Event.Button.WheelDown else Event.Button.WheelUp
  } else {
    when (cb and 3) {
      0 -> Event.Button.Button1
      1 -> Event.Button.Button2
      2 -> Event.Button.Button3
      else -> Event.Button.NoButton
    }
  }
  return MouseEventImpl
}

private object ModifierImpl : Event.Modifier {
  override var isShift: Boolean = false
  override var isAlt: Boolean = false
  override var isControl: Boolean = false
}

private object MouseEventImpl : Event.Mouse {
  override var x: Int = 0
  override var y: Int = 0

  override val modifier = ModifierImpl
  override var button: Event.Button = Event.Button.NoButton
}

private object KeyEventImpl : Event.Key {
  override var char: Char = ' '
}
*/
