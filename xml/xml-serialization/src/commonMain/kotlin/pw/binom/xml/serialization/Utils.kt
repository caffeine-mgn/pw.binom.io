package pw.binom.xml.serialization

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun String?.inArray(array: String?): Boolean = (this ?: "") == (array ?: "")

fun String?.inArray(array: Array<String>?): Boolean {
    if (array == null && this.isNullOrEmpty()) {
        return true
    }
    if (array == null) {
        return false
    }
    return if (this == null) {
        "" in array
    } else {
        this in array
    }
}

internal fun <T> a(f: suspend () -> T): T {
    var result2: Result<T>? = null
    f.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result2 = result
        }
    })
    return result2!!.getOrThrow()
}
