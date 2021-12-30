package pw.binom.strong

import pw.binom.concurrency.asReference
import pw.binom.getOrException
import pw.binom.io.use
//import pw.binom.process.Signal
import pw.binom.strong.exceptions.StartupException
import pw.binom.strong.exceptions.StrongException
/*
object StrongApplication {
    fun start(vararg config: Strong.Config, afterInit: ((Strong) -> Unit)? = null) {
        runBlocking {  }
        NetworkDispatcher().use { networkDispatcher ->
            val initProcess = networkDispatcher.startCoroutine {
                Strong.create(
                    *(arrayOf(Strong.config { it.bean { networkDispatcher } }) + config)
                ).asReference()
            }

            while (!initProcess.isDone) {
                networkDispatcher.select()
                if (initProcess.isDone && initProcess.isFailure) {
                    throw StartupException(initProcess.exceptionOrNull!!)
                }
            }
            val strong = initProcess.getOrException()
            val initFunc = if (afterInit != null) {
                runCatching { afterInit(strong.value) }
            } else {
                Result.success(Unit)
            }
            if (initFunc.isSuccess) {
                while (!Signal.isInterrupted && !strong.value.isDestroyed) {
                    networkDispatcher.select(1000)
                }
            }
            try {
                val destroyFuture = networkDispatcher.startCoroutine {
                    strong.value.destroy()
                }
                strong.close()

                while (!destroyFuture.isDone) {
                    networkDispatcher.select(100)
                }
                if (destroyFuture.isFailure) {
                    val e = StrongException("Can't destroy Strong", destroyFuture.exceptionOrNull!!)
                    if (initFunc.isFailure) {
                        e.addSuppressed(initFunc.exceptionOrNull()!!)
                    }
                    throw e
                }
            } catch (e: Throwable) {
                if (initFunc.isFailure) {
                    e.addSuppressed(initFunc.exceptionOrNull()!!)
                }
                throw e
            }
            if (initFunc.isFailure) {
                throw initFunc.exceptionOrNull()!!
            }
        }
    }
}
*/