package pw.binom.network

import pw.binom.async
import pw.binom.concurrency.*
import pw.binom.doFreeze
import kotlin.coroutines.*

//class NetworkDispatcherHolderElement(
//    val keyHolder: CrossThreadKeyHolder
//) : CrossThreadCoroutineContext, CoroutineContext.Element {
//    override val key: CoroutineContext.Key<NetworkDispatcherHolderElement>
//        get() = NetworkDispatcherHolderElementKey
//}
//
//object NetworkDispatcherHolderElementKey : CoroutineContext.Key<NetworkDispatcherHolderElement>

class NetworkHolderElement(
    val keyHolder: CrossThreadKeyHolder
) : CrossThreadCoroutineContext, CoroutineContext.Element {
    override val key: CoroutineContext.Key<NetworkHolderElement>
        get() = NetworkHolderElementKey
}

object NetworkHolderElementKey : CoroutineContext.Key<NetworkHolderElement>

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

@Suppress("UNCHECKED_CAST")
suspend fun <R> network(func: suspend () -> R): R =
    suspendCoroutine { con ->
        val holder = con.context[NetworkHolderElementKey]?.keyHolder
        if (holder == null) {
            con.resumeWithException(IllegalStateException("No defined default network key holder"))
            return@suspendCoroutine
        }
        func.doFreeze()
        val worker = con.context[CrossThreadCoroutineKey]?.crossThreadCoroutine
        if (worker == null) {
            con.resumeWithException(IllegalStateException("Can't find worker in Context"))
            return@suspendCoroutine
        }
        val workerContinuation = con.asReference().doFreeze()
        holder.waitReadyForWrite {
            async {
                try {
                    val result = runCatching { func() }
                    worker.coroutine(result, workerContinuation as Reference<Continuation<Any?>>)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
