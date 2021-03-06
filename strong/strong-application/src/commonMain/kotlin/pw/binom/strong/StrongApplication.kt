package pw.binom.strong

import pw.binom.getOrException
import pw.binom.io.use
import pw.binom.network.NetworkDispatcher
import pw.binom.process.Signal
import pw.binom.strong.exceptions.StartupException
import pw.binom.strong.exceptions.StrongException
import kotlin.time.TimeSource
import kotlin.time.measureTime

object StrongApplication {
    fun start(vararg config: Strong.Config) {
        NetworkDispatcher().use { networkDispatcher ->
            val initProcess = networkDispatcher.async {
                Strong.create(
                    *(arrayOf(Strong.config { it.define(networkDispatcher) }) + config)
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
            val destroyFuture = networkDispatcher.async {
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