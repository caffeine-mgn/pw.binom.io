package pw.binom.collections

fun <T> MutableList<T>.removeAtUsingReplace(index: Int) {
  val lastIndex = lastIndex
  if (index == lastIndex) {
    removeAt(index)
  } else {
    this[index] = this[lastIndex]
    removeAt(lastIndex)
  }
}

inline fun <E> MutableCollection<E>.removeIf(condition: (E) -> Boolean) {
  val it = iterator()
  while (it.hasNext()) {
    val e = it.next()
    if (condition(e)) {
      it.remove()
    }
  }
}
