package pw.binom.xml

class CircularCounter(override val capacity: Int) : Circular() {
  val headCursor
    get() = head
  val tailCursor
    get() = tail

  fun push() = calcPush()
  fun pop() = calcPop()
}
