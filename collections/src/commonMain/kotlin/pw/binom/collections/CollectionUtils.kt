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
