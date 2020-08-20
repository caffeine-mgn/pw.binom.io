package pw.binom.thread

import pw.binom.io.Closeable
import kotlin.coroutines.Continuation

//interface ThreadPool : Closeable {
//    suspend fun <T> executeAsync(func: () -> T): T
//    fun execute(func: () -> Unit)
//    fun <T> resume(continuation: Continuation<T>, result: Result<T>)
//    fun shutdown()
//}