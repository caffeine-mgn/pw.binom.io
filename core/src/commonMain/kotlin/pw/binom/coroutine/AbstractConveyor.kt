package pw.binom.coroutine

import pw.binom.io.ClosedException
import kotlin.coroutines.*

//private abstract class AbstractConveyor<T> : Conveyor<T> {
//    private var consumer: Continuation<T>? = null
//    override var isFinished = false
//    override var exceptionOrNull: Throwable? = null
//    protected open suspend fun consume(): T {
//        if (isFinished) {
//            throw ClosedException()
//        }
//        if (consumer != null) {
//            throw IllegalStateException("Already consuming")
//        }
//        return suspendCoroutine {
//            consumer = it
//        }
//    }
//
//    override suspend fun exception(exception: Throwable) {
//        if (isFinished) {
//            throw ClosedException()
//        }
//        val consumer = consumer ?: throw IllegalStateException("Conveyor not consume any value")
//        this.consumer = null
//        consumer.resumeWithException(exception)
//    }
//
//    override suspend fun submit(value: T) {
//        if (isFinished) {
//            throw ClosedException()
//        }
//        val consumer = consumer ?: throw IllegalStateException("Conveyor not consume any value")
//        this.consumer = null
//        consumer.resume(value)
//    }
//
//    override suspend fun asyncClose() {
//        exception(ClosedException())
//    }
//}

interface Consumer<T> {
    suspend fun consume(): T
}

//suspend fun <T> conveyor(func: suspend Consumer<T>.() -> Unit): Conveyor<T> {
//    val c = object : AbstractConveyor<T>(), Consumer<T> {
//        override suspend fun consume(): T =
//            super.consume()
//    }
//
//    val currentContext = suspendCoroutine<CoroutineContext> {
//        it.resume(it.context)
//    }
//    val dd: suspend () -> Unit = {
//        func(c)
//    }
//    dd.startCoroutine(object : Continuation<Unit> {
//        override val context: CoroutineContext
//            get() = currentContext
//
//        override fun resumeWith(result: Result<Unit>) {
//            if (!c.isFinished) {
//                c.exceptionOrNull = result.exceptionOrNull()
//            }
//            c.isFinished = true
//        }
//    })
//    return c
//}