package pw.binom.network

import pw.binom.async
import pw.binom.concurrency.*
import pw.binom.doFreeze
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class CrossThreadKeyHolder(val key: Selector.Key) {
    val readyForWriteListener = ConcurrentQueue<() -> Unit>()
    private val networkThread = ThreadRef()

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
}

suspend fun <R> CrossThreadKeyHolder.executeOnNetwork(func: suspend () -> R): R =
    suspendCoroutine { con ->
        func.doFreeze()
        val worker = con.context[WorkerHolderElementKey]?.worker
            ?: throw IllegalStateException("Can't find worker in Context")
        val workerContinuation = con.asReference().doFreeze()
        waitReadyForWrite {
            async {
                try {
                    val result = runCatching { func() }
                    worker.resume(result, workerContinuation)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
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
                ?: it.context[ExecutorPoolHolderKey]?.executor
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