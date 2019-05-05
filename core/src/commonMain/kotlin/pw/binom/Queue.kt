package pw.binom

interface Queue<T> {
    val isEmpty: Boolean
    fun pop(): T
}

interface AppendableQueue<T> : Queue<T> {
    fun push(value: T)
    fun peek(): T
}

/**
 * Wait a value and returns it
 *
 * @param timeout timeout for wait a value
 * @throws QueuePopTimeout when timeout is done
 */
fun <T> Queue<T>.popAwait(timeout: Long? = null): T {
    if (timeout == null) {
        while (isEmpty) {
            Thread.sleep(1)
        }
    } else {
        val start = Thread.currentTimeMillis()
        while (isEmpty) {
            if (Thread.currentTimeMillis() - start > timeout)
                throw QueuePopTimeout()
            Thread.sleep(1)
        }
    }
    return pop()
}

fun <T> Queue<T>.popOrNull(): T? {
    if (isEmpty)
        return null
    return pop()
}

class QueuePopTimeout : Exception()