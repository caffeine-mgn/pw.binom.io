package pw.binom.io.httpServer

import pw.binom.*
import pw.binom.concurrency.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.NetworkAddress
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

//class MultiThreading {
//
//    @Ignore
//    @OptIn(ExperimentalTime::class)
//    @Test
//    fun test() {
//        val worker = WorkerPool(10)
//        val nd = NetworkDispatcher()
//        val port = Random.nextInt(1000, Short.MAX_VALUE - 1)
//        val data = ByteArray(120) { it.toByte() }
//        val t = DeadlineTimer.create()
//        val server = HttpServer(nd, handler = Handler { r ->
//            val resp = r.response()
//            resp.status = 200
//            val dataBuffer = ByteBuffer.wrap(data).clean()
////            resp.headers.contentLength = dataBuffer.remaining.toULong()
//            t.delay(Duration.seconds(1))
//            resp.sendBinary(dataBuffer)
//        })
//
//        server.bindHttp(NetworkAddress.Immutable("127.0.0.1", port))
//
//        suspend fun makeCall(name: String) {
//            BaseHttpClient(nd).use { client ->
//                try {
//                    println("Try make request $name...")
//                    client.connect(
//                        method = HTTPMethod.GET.code,
//                        uri = "http://127.0.0.1:$port/$name".toURI(),
//                    ).getResponse().use { response ->
//                        println("$name reading...${response.responseCode}  ${response.headers.contentLength}")
//                        println("$name ok->1")
//                        val oo = ByteArrayOutput()
//                        response.readData().use { it.copyTo(oo) }
//                        val buf = oo.data
//                        buf.flip()
//                        if (data.size != buf.remaining) {
//                            println("Invalid body")
//                            buf.forEachIndexed { index, value ->
//                                println("$index -> $value = ${data[index] == value}")
//                            }
//                        }
//                        assertEquals(data.size, buf.remaining)
//                        println("$name ok->2")
//
//                        println("$name ok->2")
//                        val dataFromServer = buf.toByteArray()
//                        println("$name ok->3")
//                        data.forEachIndexed { index, byte ->
//                            assertEquals(byte, dataFromServer[index])
//                        }
//                        println("$name ok->4")
//                        println("$name ok!")
//                    }
//                } catch (e: Throwable) {
//                    println("Errpr on $name")
//                    e.printStackTrace()
//                    throw e
//                }
//            }
//        }
//
//        val inOrder = nd.startCoroutine {
//            try {
//                println("Start in order")
//                val totalTime = measureTime {
//                    println("prepare inOrder-1")
//                    val callTime1 = measureTime { makeCall("inOrder-1") }
//                    println("prepare inOrder-2")
//                    val callTime2 = measureTime { makeCall("inOrder-2") }
//                    assertTrue("callTime1=$callTime1") { callTime1 > 0.1.seconds && callTime1 < 2.0.seconds }
//                    assertTrue(callTime2 > 0.1.seconds && callTime2 < 2.0.seconds)
//                }
//                println("inOrder done!")
//                assertTrue(totalTime > 1.5.seconds && totalTime < 3.0.seconds)
//            } catch (e: Throwable) {
//                println("Error on inOrder")
//                e.printStackTrace()
//                throw e
//            }
//        }
////        val inParallel=Future2.success(Future2.success(1.0.seconds))
//        val inParallel = nd.startCoroutine {
//            val callTime1 = fork { makeCall("inParallel-1") }
//            val callTime2 = fork { makeCall("inParallel-2") }
//            measureTime {
//                callTime1.join()
//                callTime2.join()
//            }
////            val totalTime = worker.submit {
////                measureTime {
////                    callTime1.join()
////                    callTime2.join()
////                }
////            }
////            totalTime
//        }
//
//
//
//        while (!inOrder.isDone || !inParallel.isDone) {
//            nd.select(500)
//        }
//        inOrder.join()
//        val totalTime = inParallel.join()
//        assertTrue(totalTime > 0.1.seconds && totalTime < 2.0.seconds)
//    }
//}

fun <T> Future<T>.join(deley: Long = 50L): T {
    while (!isDone) {
        sleep(deley)
    }
    if (isFailure) {
        throw exceptionOrNull!!
    }
    return resultOrNull as T
}