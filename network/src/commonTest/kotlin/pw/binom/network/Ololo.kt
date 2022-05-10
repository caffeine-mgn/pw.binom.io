package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.io.bufferedReader
import pw.binom.io.bufferedWriter
import pw.binom.io.use
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Ignore
import kotlin.test.Test

suspend fun getDispatcher(): CoroutineDispatcher? =
    suspendCoroutine {
        it.resume(it.context[ContinuationInterceptor] as CoroutineDispatcher?)
    }

@Ignore
class Ololo {

    @Test
    fun test() = runTest {
        val network = NetworkCoroutineDispatcherImpl()
        val ccc = TcpServerSocketChannel()
        println("Bind on 8335")
        ccc.bind(NetworkAddress.Immutable(port = 8335))
        println("Wait clients...")
        ccc.use {
            val server = network.attach(ccc)
            while (true) {
                val newClient = server.accept()
                launch {
                    val line = newClient.bufferedReader(closeParent = false).use { it.readln() }
                    newClient.bufferedWriter(closeParent = false).use { it.append("Echo $line") }
                    newClient.asyncClose()
                }
            }
        }

        println("Execute in default ${getDispatcher()}")
        val c = getDispatcher()!!

        withContext(network) {
            println("Execute in network ${getDispatcher()} 1")
            withContext(Dispatchers.Default) {
                println("Execute in default inside network ${getDispatcher()}")
//                network.sss(c)
            }
            println("Execute in network ${getDispatcher()} 2")
//                network.sss(c)
        }
        println("Execute in default ${getDispatcher()}")
//
//            Network().invoke {
//                println("1 ololo!  -> ${getDispatcher()}")
//            }
//            launch(start = CoroutineStart.UNDISPATCHED) {
//
//            }
//            println("2 ololo!    -> ${getDispatcher()}")
    }
}
