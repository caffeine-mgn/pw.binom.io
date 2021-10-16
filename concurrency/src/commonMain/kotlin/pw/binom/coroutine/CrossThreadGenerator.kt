package pw.binom.coroutine

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.asReference
import pw.binom.doFreeze

private class CrossThreadGenerator<T> constructor(val dispatcher: Dispatcher, generator: Generator<T>) : Generator<T> {
    companion object {
        internal suspend fun <T> wrap(generator: Generator<T>): CrossThreadGenerator<T> {
            if (generator is CrossThreadGenerator) {
                return generator
            }
            val dispatcher = Dispatcher.getCurrentDispatcher()
                ?: throw IllegalStateException("Generator created outside any dispatcher")
            return CrossThreadGenerator(dispatcher = dispatcher, generator = generator)
        }
    }

    private val finished = AtomicBoolean(false)
    private val generatorReference = generator.asReference()

    override val isFinished: Boolean
        get() = finished.value

    override suspend fun next(): T {
        if (finished.value) {
            throw NoSuchElementException()
        }
        return dispatcher.start {
            try {
                val r = generatorReference.value.next()
                finished.value = generatorReference.value.isFinished
                if (finished.value) {
                    generatorReference.close()
                }
                r
            } catch (e: Throwable) {
                finished.value = true
                generatorReference.close()
                throw e
            }
        }
    }

    override suspend fun finish() {
        dispatcher.start {
            val g = generatorReference.value
            try {
                while (g.isFinished) {
                    g.next()
                }
            } finally {
                finished.value = true
                generatorReference.close()
            }
        }
    }

    init {
        doFreeze()
    }
}

/**
 * Creates ThreadSafe [Generator]. Should be called from dispatcher managed coroutine. New genrator will be linked with
 * current coroutine dispatcher
 */
suspend fun <T : Any> Dispatcher.generator(func: suspend Yieldable<T>.() -> T): Generator<T> {
    func.doFreeze()
    val dispatcher=this
    return start {
        val generator = pw.binom.coroutine.generator(func)
        CrossThreadGenerator(dispatcher = dispatcher, generator = generator)
    }
}

/**
 * Creates ThreadSafe [Generator]. Should be called from dispatcher managed coroutine. New genrator will be linked with
 * current coroutine dispatcher
 */
suspend fun <T : Any> crossThreadGenerator(func: suspend Yieldable<T>.() -> T): Generator<T> {
    func.doFreeze()
    val dispatcher =
        Dispatcher.getCurrentDispatcher() ?: throw IllegalStateException("Execution outside any dispatcher")
    return dispatcher.generator(func)
}
