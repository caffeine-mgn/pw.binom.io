package pw.binom.xml

abstract class Circular {
  protected var head = 0
    private set
  protected var tail = 0
    private set
  var size = 0
    private set

  protected abstract val capacity: Int

  val isEmpty
    get() = head == tail

  val isNotEmpty
    get() = head != tail

  protected fun calcPush(): Int {
    val resultIndex = head % capacity
    head++
    if (size == capacity) {
      tail++
    }
    size = minOf(size + 1, capacity)
    return resultIndex
  }

  protected fun calcPop(): Int {
    if (head == tail) {
      throw NoSuchElementException()
    }
    head--
    size--
    return head % capacity
  }
}
