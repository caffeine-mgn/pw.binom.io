package pw.binom.strong

import pw.binom.getOrException
import pw.binom.io.use
import pw.binom.network.NetworkDispatcher
import pw.binom.process.Signal
import pw.binom.strong.exceptions.StartupException
import pw.binom.strong.exceptions.StrongException

object StrongApplication {
    fun start(vararg config: Strong.Config) {
        NetworkDispatcher().use { networkDispatcher ->
            val initProcess = networkDispatcher.startCoroutine {
                Strong.create(
                    *(arrayOf(Strong.config { it.bean { networkDispatcher } }) + config)
                )
            }

            while (!initProcess.isDone) {
                networkDispatcher.select()
                if (initProcess.isDone && initProcess.isFailure) {
                    throw StartupException(initProcess.exceptionOrNull!!)
                }
            }
            val strong = initProcess.getOrException()
            while (!Signal.isInterrupted) {
                networkDispatcher.select(1000)
            }
            val destroyFuture = networkDispatcher.startCoroutine {
                strong.destroy()
            }

            while (!destroyFuture.isDone) {
                networkDispatcher.select(100)
            }
            if (destroyFuture.isFailure) {
                throw StrongException("Can't destroy Strong", destroyFuture.exceptionOrNull!!)
            }
        }
    }
}