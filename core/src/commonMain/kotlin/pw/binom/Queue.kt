package pw.binom

@Suppress("UNCHECKED_CAST")
class PopResult<T> {
    val isEmpty: Boolean
        get() = empty
    val value: T
        get() {
            if (isEmpty)
                throw IllegalStateException("PopResult is Empty")
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

    init {
        neverFreeze()
    }
}

interface Queue<T> {
    val isEmpty: Boolean
    val size: Int
    fun pop(): T
    fun pop(dist: PopResult<T>)
}

interface AppendableQueue<T> : Queue<T> {
    fun push(value: T)
    fun peek(): T
}
/*
class PopTimeoutException : RuntimeException()

fun <T> Queue<T>.popAwait(timeout: Long? = null): T {
    val dist = PopResult<T>()
    if (timeout == null) {
        do {
            pop(dist)
            if (dist.isEmpty) {
                Thread.sleep(1)
                continue
            } else {
                return dist.value
            }
        } while (true)
    } else {
        val start = Thread.currentTimeMillis()
        do {
            pop(dist)
            if (!dist.isEmpty)
                return dist.value

            if (Thread.currentTimeMillis() - start > timeout)
                throw PopTimeoutException()

            Thread.sleep(1)
        } while (true)
    }
}

/**
 * Wait a value and returns it
 *
 * @param timeout timeout for wait a value
 */
fun <T> Queue<T>.popAwait(dist: PopResult<T>, timeout: Long? = null) {
    if (timeout == null) {
        while (isEmpty) {
            Thread.sleep(1)
        }
    } else {
        val start = Thread.currentTimeMillis()
        do {
            pop(dist)
            if (!dist.isEmpty)
                return
            if (Thread.currentTimeMillis() - start > timeout)
                return
            Thread.sleep(1)
        } while (dist.isEmpty)
    }
}
*/
fun <T> Queue<T>.popOrNull(): T? {
    val result = PopResult<T>()
    pop(result)
    if (result.isEmpty)
        return null
    return result.value
}

fun <T> Queue<T>.popOrElse(func: () -> T): T {
    val result = PopResult<T>()
    pop(result)
    if (result.isEmpty)
        return func()
    return result.value
}