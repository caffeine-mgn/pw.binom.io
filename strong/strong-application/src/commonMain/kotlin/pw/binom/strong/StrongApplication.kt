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
            println("Starting Strong")
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
            println("Strong started! Wait until done!")

            while (!Signal.isInterrupted) {
                networkDispatcher.select()
            }

            val destroyFuture = networkDispatcher.async {
                strong.destroy()
            }

            while (!destroyFuture.isDone) {
                networkDispatcher.select()
            }
            if (destroyFuture.isFailure) {
                throw StrongException("Can't destroy Strong", destroyFuture.exceptionOrNull!!)
            }
        }
    }
}