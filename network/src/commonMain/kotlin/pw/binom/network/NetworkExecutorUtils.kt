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
