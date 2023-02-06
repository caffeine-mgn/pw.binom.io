package pw.binom.collections

interface Queue<T> {
    val isEmpty: Boolean
    val size: Int
    fun pop(): T
    fun pop(dist: PopResult<T>)

    fun peek(): T
//    fun peek(dest: PopResult<T>): Boolean

    fun popOrNull(): T? {
        val result = PopResult<T>()
        pop(result)
        if (result.isEmpty) {
            return null
        }
        return result.value
    }

    fun popOrElse(func: () -> T): T {
        val result = PopResult<T>()
        pop(result)
        if (result.isEmpty) {
            return func()
        }
        return result.value
    }
}
