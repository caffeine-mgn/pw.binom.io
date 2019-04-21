package pw.binom.job

import pw.binom.io.Closeable
import kotlin.native.concurrent.*

internal actual class PlatfromExecutor actual constructor() : Closeable {
    override fun close() {
        native.requestTermination(false)
    }

    private val native = Worker.start()

    private class WorkerResult<T : Any?> {
        var result: Any? = null
        var error = false

        fun error(e: Throwable): WorkerResult<T> {
            error = true
            result = e
            return this
        }

        fun succsses(value: T): WorkerResult<T> {
            result = value
            return this
        }
    }

    private class WorkerParams<T, R>(val params: T, val func: (T) -> R)

    actual fun <T, R> execute(param: T, f: (T) -> R): Promise<R> {
        val p = Promise<R>()

        native.execute(TransferMode.SAFE, { WorkerParams(param, f).freeze() }) {
            println("1")
            println("2")
            try {
                println("3")
                WorkerResult<R>().succsses(it.func(it.params))
            } catch (e: Throwable) {
                WorkerResult<R>().error(e)
            }
        }.consume {
            println("->1")
            println("->2")
            if (it.error) {
                println("->3")
                p.exception(it.result as Throwable)
                println("->4")
            }else {
                println("->5")
                p.resume(it.result as R)
                println("->6")
            }
        }
        return p
    }
}