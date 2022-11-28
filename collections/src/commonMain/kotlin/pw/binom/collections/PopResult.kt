package pw.binom.collections

@Suppress("UNCHECKED_CAST")
class PopResult<T> {
    val isEmpty: Boolean
        get() = empty
    val value: T
        get() {
            if (isEmpty) {
                error("PopResult is Empty")
            }
            return _value as T
        }

    private var _value: T? = null
    private var empty = true

    fun set(value: T) {
        _value = value
        empty = false
    }

    fun clear() {
        _value = null
        empty = true
    }
}
