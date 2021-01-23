package pw.binom.io.httpServer

import pw.binom.*
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.*
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.use
import pw.binom.network.*
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

            println("--#1")
            execute {
                println("--#2")
                Worker.sleep(1_000)
                println("--#3")
                network {
                    println("--#4")
                    val sent = completer.value.write(dataBuffer)
                    completer.value.flush()
                    println("--#5: sent $sent")
                }
                println("--#6")
            }
            println("--#7")
            completer.close()
        })

        server.bindHTTP(NetworkAddress.Immutable("127.0.0.1", port))

        suspend fun makeCall(name: String) {
            val client = AsyncHttpClient(nd)
            try {
                client.request(
                    method = "GET",
                    url = URL("http://127.0.0.1:$port")
                ).response().use { response ->
                    println("$name reading...")
                    val buf = ByteBuffer.alloc(60).clean()
                    println("$name ok->1")
                    assertEquals(data.size, response.read(buf))
                    println("$name ok->2")
                    buf.flip()
                    println("$name ok->2")
                    val dataFromServer = buf.toByteArray()
                    println("$name ok->3")
                    data.forEachIndexed { index, byte ->
                        assertEquals(byte, dataFromServer[index])
                    }
                    println("$name ok->4")
                    println("$name ok!")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            } finally {
                client.close()
            }
        }

        val inOrder = nd.async(worker) {
            try {
                println("Start in order")
                val totalTime = measureTime {
                    println("#1")
                    val callTime1 = measureTime { makeCall("inOrder-1") }
                    println("#2")
                    val callTime2 = measureTime { makeCall("inOrder-2") }
                    assertTrue(callTime1 > 0.1.seconds && callTime1 < 2.0.seconds)
                    assertTrue(callTime2 > 0.1.seconds && callTime2 < 2.0.seconds)
                }
                println("inOrder done!")
                assertTrue(totalTime > 1.5.seconds && totalTime < 3.0.seconds)
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
//        val inParallel=Future2.success(Future2.success(1.0.seconds))
        val inParallel = nd.async(worker) {
            val callTime1 = nd.async(worker) { makeCall("inParallel-1") }
            val callTime2 = nd.async(worker) { makeCall("inParallel-2") }
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