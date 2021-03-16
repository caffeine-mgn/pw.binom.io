package pw.binom.network

import pw.binom.async
import pw.binom.concurrency.*
import pw.binom.doFreeze
import pw.binom.io.Closeable
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class CrossThreadKeyHolder(val key: Selector.Key) : CrossThreadCoroutine, Closeable {
    val readyForWriteListener = ConcurrentQueue<() -> Unit>()
    private val networkThread = ThreadRef()
    val isNetworkThread
        get() = networkThread.same

    init {
        doFreeze()
    }

    fun waitReadyForWrite(func: () -> Unit) {
        if (networkThread.same) {
            func()
        } else {
            readyForWriteListener.push(func.doFreeze())
            key.addListen(Selector.OUTPUT_READY)
        }
    }

    override fun coroutine(result: Result<Any?>, continuation: Reference<Continuation<Any?>>) {
        waitReadyForWrite {
            continuation.free().resumeWith(result)
        }
    }

    override fun close() {
        readyForWriteListener.close()
    }
}

fun <T> Worker.resume(result: Result<T>, func: Reference<Continuation<T>>) {
    execute(result to func) {
        val f = it.second.value
        it.second.close()
        f.resumeWith(it.first)
    }
}

suspend fun <R> CrossThreadKeyHolder.executeOnExecutor(executor: WorkerPool? = null, func: suspend () -> R) {
    suspendCoroutine<R> {
        val executorPool =
            executor
                ?: it.context[ExecutorServiceHolderKey]?.executor
                ?: throw IllegalStateException("No defined default worker")
        val selfCon = it.asReference()
        executorPool.submitAsync {
            val result = runCatching { func.invoke() }
            this.waitReadyForWrite {
                val self = selfCon.value
                selfCon.close()
                self.resumeWith(result)
            }
        }
    }
}