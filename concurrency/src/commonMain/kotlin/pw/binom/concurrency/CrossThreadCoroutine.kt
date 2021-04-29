package pw.binom.concurrency

import pw.binom.doFreeze
import kotlin.coroutines.*

/**
 * Object must be frozen, because it will be pass to other thread
 */
fun interface CrossThreadCoroutine {
    fun coroutine(result: Result<Any?>,continuation:Reference<Continuation<Any?>>)
}

suspend fun <R> execute(executor: WorkerPool? = null, func: suspend () -> R): R =
    suspendCoroutine {
        val executorPool =
            executor
                ?: it.context[ExecutorServiceHolderKey]?.executor
        val newContext = it.context.buildCrossThreadContext()
        if (executorPool == null) {
            it.resumeWithException(IllegalStateException("No defined default worker"))
            return@suspendCoroutine
        }

        val dispatcher = it.context[CrossThreadCoroutineKey]?.crossThreadCoroutine
        if (dispatcher == null) {
            it.resumeWithException(IllegalStateException("No defined default CrossThreadCoroutineKey. Make sure you start Coroutine using NetworkDispatcher"))
            return@suspendCoroutine
        }
        val holder = dispatcher.doFreeze()
        val selfCon = it.asReference()
        executorPool.submitAsync(newContext.doFreeze()) {
            val result = runCatching { func.invoke() }
            holder.coroutine(result = result, continuation = selfCon as Reference<Continuation<Any?>>)
        }
    }

suspend fun <T> execute(worker: Worker, func: suspend () -> T): T =
    suspendCoroutine { con ->
        val dispatcher = con.context[CrossThreadCoroutineKey]?.crossThreadCoroutine
        if (dispatcher == null) {
            con.resumeWithException(IllegalStateException("No defined default CrossThreadCoroutineKey"))
            return@suspendCoroutine
        }

        val newContext = con.context.buildCrossThreadContext()
        newContext.doFreeze()
        val conRef = con.asReference()
        worker.execute(Unit) {
            func.startCoroutine(object : Continuation<T> {
                override val context: CoroutineContext = newContext + CrossThreadCoroutineElement(worker)

                override fun resumeWith(result: Result<T>) {
                    dispatcher.coroutine(result, conRef as Reference<Continuation<Any?>>)
                }
            })
        }
    }