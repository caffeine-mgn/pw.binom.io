package pw.binom.io.httpServer

import pw.binom.*
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.*
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.executeOnExecutor
import pw.binom.network.executeOnNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

class MultiThreading {

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        var paralellThreadCounter = AtomicInt(0)
        val worker = WorkerPool(10)
        val nd = NetworkDispatcher()
        val port = Random.nextInt(1000, Short.MAX_VALUE - 1)
        val data = Random.nextBytes(30)
        val server = HttpServer(nd, executor = worker, handler = Handler { req, resp ->
            resp.enableCompress = true
            resp.status = 200
            val dataBuffer = ByteBuffer.wrap(data).clean().doFreeze()
            resp.addHeader(Headers.CONTENT_LENGTH, dataBuffer.toString())
            val completer = resp.complete().asReference()
            val holder = req.keyHolder
            paralellThreadCounter.increment()
            req.keyHolder.executeOnExecutor {
                Worker.sleep(1_000)
                holder.executeOnNetwork {
                    val sent = completer.value.write(dataBuffer)
                    completer.value.flush()
                }
            }
            completer.close()
            paralellThreadCounter.decrement()
        })

        server.bindHTTP(NetworkAddress.Immutable("127.0.0.1", port))

        suspend fun makeCall() {
            val client = AsyncHttpClient(nd)
            client.request(
                method = "GET",
                url = URL("http://127.0.0.1:$port")
            ).response().use { response ->
                val buf = ByteBuffer.alloc(60).clean()
                assertEquals(data.size, response.read(buf))
                buf.flip()
                val dataFromServer = buf.toByteArray()
                data.forEachIndexed { index, byte ->
                    assertEquals(byte, dataFromServer[index])
                }
            }
        }

        val inOrder = async2 {
            val totalTime = measureTime {
                val callTime1 = measureTime { makeCall() }
                val callTime2 = measureTime { makeCall() }
                assertTrue(callTime1 > 0.1.seconds && callTime1 < 2.0.seconds)
                assertTrue(callTime2 > 0.1.seconds && callTime2 < 2.0.seconds)
            }
            assertTrue(totalTime > 1.5.seconds && totalTime < 3.0.seconds)
        }
        val inParallel = async2 {
            val callTime1 = async2 { makeCall() }
            val callTime2 = async2 { makeCall() }
            val totalTime = worker.submit {
                measureTime {
                    callTime1.join()
                    callTime2.join()
                }
            }
            totalTime
        }



        while (!inOrder.isDone || !inParallel.isDone) {
            nd.select(500)
        }
        inOrder.join()
        val totalTime = inParallel.join().join()
        assertTrue(totalTime > 0.1.seconds && totalTime < 2.0.seconds)
    }
}

fun <T> Future2<T>.join(deley: Long = 50L): T {
    while (!isDone) {
        Worker.sleep(deley)
    }
    if (isFailure) {
        throw exceptionOrNull!!
    }
    return resultOrNull as T
}