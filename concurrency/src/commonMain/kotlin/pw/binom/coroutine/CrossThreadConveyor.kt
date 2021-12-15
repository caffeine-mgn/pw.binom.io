package pw.binom.coroutine

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.concurrency.asReference
import pw.binom.doFreeze
import pw.binom.io.ClosedException

//private class CrossThreadConveyor<T> constructor(val dispatcher: Dispatcher, generator: Conveyor<T>) : Conveyor<T> {
//    private val generatorReference = generator.asReference()
//    private val finished = AtomicBoolean(false)
//    private val exception = AtomicReference<Throwable?>(null)
//    override val isFinished: Boolean
//        get() = finished.value
//
//    override val exceptionOrNull: Throwable?
//        get() = exception.value
//
//    override suspend fun submit(value: T) {
//        if (finished.value) {
//            throw ClosedException()
//        }
//        value?.doFreeze()
//        dispatcher.start {
//            try {
//                generatorReference.value.submit(value)
//                finished.value = generatorReference.value.isFinished
//                exception.value = generatorReference.value.exceptionOrNull
//                if (finished.value) {
//                    generatorReference.close()
//                }
//            } catch (e: Throwable) {
//                finished.value = true
//                generatorReference.close()
//                throw e
//            }
//        }
//    }
//
//    override suspend fun exception(exception: Throwable) {
//        if (finished.value) {
//            throw ClosedException()
//        }
//        exception.doFreeze()
//        dispatcher.start {
//            generatorReference.value.exception(exception)
//        }
//    }
//
//    override suspend fun asyncClose() {
//        exception(ClosedException())
//    }
//}

/**
 * Creates ThreadSafe [Conveyor]. Should be called from dispatcher managed coroutine. New conveyor will be linked with
 * current coroutine dispatcher
 */
//suspend fun <T : Any> Dispatcher.conveyor(func: suspend Consumer<T>.() -> Unit): Conveyor<T> {
//    func.doFreeze()
//    val dispatcher=this
//    return start {
//        val generator = pw.binom.coroutine.conveyor(func)
//        CrossThreadConveyor(dispatcher = dispatcher, generator = generator)
//    }
//}

/**
 * Creates ThreadSafe [Generator]. Should be called from dispatcher managed coroutine. New genrator will be linked with
 * current coroutine dispatcher
 */
//suspend fun <T : Any> crossThreadConveyor(func: suspend Consumer<T>.() -> Unit): Conveyor<T> {
//    func.doFreeze()
//    val dispatcher =
//        Dispatcher.getCurrentDispatcher() ?: throw IllegalStateException("Execution outside any dispatcher")
//    return dispatcher.conveyor(func)
//}
