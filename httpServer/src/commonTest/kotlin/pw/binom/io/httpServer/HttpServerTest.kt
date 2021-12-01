package pw.binom.io.httpServer

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pw.binom.concurrency.WorkerPool
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.test.Test

class HttpServerTest {

    @Test
    fun test() {
        val oo = WorkerPool(10)
        val server = HttpServer(handler = Handler {
            it.response {
                it.status = 202
                it.headers.contentType = "text/html;charset=utf-8"
                it.startWriteText().use {
                    it.append("Hello! Привет в UTF-8   disoather: ${coroutineContext[ContinuationInterceptor]}")
                    withContext(oo){
                        it.append("и ${kotlin.coroutines.coroutineContext[ContinuationInterceptor]}")
                    }
                }
            }
        })

        val listenJob = server.listenHttp(address = NetworkAddress.Immutable(port=8003))
        runBlocking {
//            var closed = false
            launch {
                println("Wait 10 sec")
                delay(10_000)
                println("closing server...")
                listenJob.cancel()
                println("server closed")
//                closed=true
            }
            listenJob.join()
//            while (!listenJob) {
//                delay(1_000)
//            }
        }
    }
}