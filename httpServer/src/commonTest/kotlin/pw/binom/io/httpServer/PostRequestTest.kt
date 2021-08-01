package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.copyTo
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.SocketClosedException
import kotlin.jvm.Volatile
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

//@Ignore
class PostRequestTest {
    @Volatile
    private var done = false

//    @Ignore
//    @OptIn(ExperimentalTime::class)
//    @Test
//    fun server() {
//        val bufPool = ByteBufferPool(10, 1024u * 1024u * 2u)
//        val manager = NetworkDispatcher()
//        var done = false
//        var dd = TimeSource.Monotonic.markNow()
//        val server = HttpServer3(
//            manager, object : Handler3Deprecated {
//                override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
//                    req.headers.forEach { k ->
//                        k.value.forEach {
//                            println("${k.key}: $it")
//                        }
//                    }
//                    val input = req.multipart(bufPool)
//                    if (input != null) {
//                        while (input.next()) {
//                            println("${input.formName}: [${input.utf8Reader().readText()}]")
//                        }
//                    }
//
////                val text = req.input.utf8Reader().readText()
//
////                val p = ByteBuffer.alloc(200)
////                req.input.read(p)
////                p.flip()
////                if (p.remaining > 0) {
////                    println("Readed:")
////                    (p.position until p.limit).forEach {
////                        println("0x${p[it].toString(16).toUpperCase()?.let { if (it.length == 1) "0$it" else it }} ")
////                    }
////                }
//                    println("Время простоя: ${dd.elapsedNow()}")
//                    var readFrom = Duration.ZERO
//                    var writeTo = Duration.ZERO
//                    if (req.uri == "/stop") {
//                        done = true
//                        resp.status = 204
//                        return
//                    }
//                    println("Try to return stub file!")
//                    resp.status = 200
//                    try {
//                        val filePath = "E:\\Temp\\3\\33.stl"
////                    val filePath = "/home/subochev/tmp/33.stl"
////                    val filePath = "/mnt/e/Temp/2/33.stl"
////                    val filePath="E:\\Temp\\2\\out.txt"
//                        println("Read file and copy it")
//                        File(filePath).channel(AccessType.READ).use {
//                            it.copyTo(resp.complete(1024 * 1024 * 2), bufPool)
//                        }
//                        println("File writed!")
//                    } catch (e: SocketClosedException) {
//                        throw e
//                    } catch (e: Throwable) {
//                        e.printStackTrace()
//                    }
//                    dd = TimeSource.Monotonic.markNow()
////                resp.output.write(data, 1024 * 1024)
////                resp.output.flush()
//
////                resp.output.writeln("Hello from Server")
////                resp.output.flush()
////                resp.output.close()
////                resp.output.use {
////                    val ap = it.utf8Appendable()
////                    ap.append("Hello from Server")
////                    it.flush()
////                }
//                }
//            },
//            inputBufferSize = DEFAULT_BUFFER_SIZE * 40,
//            zlibBufferSize = 512,
//            outputBufferSize = DEFAULT_BUFFER_SIZE * 40,
//            poolSize = 10
//        )
//        try {
//            server.bindHTTP(NetworkAddress.Immutable(port = 8080))
//            while (!done) {
//                manager.select(1000)
//            }
//        } catch (e: Throwable) {
//            println("Error!")
////            e.printStacktrace(Console.std)
//            throw e
//        }
//    }
/*
    @Ignore
    @Test
    fun rff() {
        val manager = SingleThreadNioManager(TODO(), TODO())
        var done = false
        val server = HttpServer(manager, object : Handler {
            override suspend fun request(req: HttpRequest, resp: HttpResponse) {
                resp.status = 200
                resp.resetHeader(Headers.SERVER, "Simple Server")
                resp.resetHeader(Headers.CONTENT_TYPE, "text/html")
                val o = resp.complete()
                o.utf8Appendable().append("Hello from simple server!")
                Console.std.appendln("Response sent1")
                Console.err.appendln("Response sent2")
            }

        })
        server.bindHTTP(port = 8080)
        while (!done) {
            manager.update()
        }
    }

    @Ignore
    @Test
    fun test() {
        val manager = SingleThreadNioManager(TODO(), TODO())

        class H : Handler {
            override suspend fun request(req: HttpRequest, resp: HttpResponse) {
                resp.status = 200
                println("Request!")
                val txt = req.input.utf8Reader().readText()
            }
        }

        val b = HttpServer(manager, H())
        b.bindHTTP("127.0.0.1", 3344)


        val t = Thread(Runnable {
            val manager = SingleThreadNioManager(TODO(), TODO())
            async {
                val client = AsyncHttpClient(manager)
                client.use {
                    client.request("POST", URL("http://127.0.0.1:3344")).use {
                        println("Try to get request!")
                        val data = ByteDataBuffer.alloc(1024)
                        it.addRequestHeader(Headers.CONTENT_LENGTH, data.size.toString())
                        it.outputStream.write(data)

                        val txt = it.inputStream.utf8Reader().readText()
                    }
                }
            }

            val start = Thread.currentTimeMillis()
            while (!Thread.currentThread.isInterrupted) {
                if (Thread.currentTimeMillis() - start > 3_000)
                    break
                manager.update(100)
            }
        })
        t.start()
        println("#1")

        val start = Thread.currentTimeMillis()
        while (true) {
            if (Thread.currentTimeMillis() - start > 3_000)
                break
            manager.update(100)
        }
        println("#2")
        t.interrupt()
        t.join()
        println("#3")
    }*/

//    @Ignore
//    @Test
//    fun testDownload() {
//        val nd = NetworkDispatcher()
//        val buf = ByteBufferPool(10, (DEFAULT_BUFFER_SIZE * 10).toUInt())
//        val server = HttpServer3(
//            manager = nd,
//            handler = object : Handler3Deprecated {
//                override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
//                    resp.status = 200
//                    File("C:\\TEMP\\6\\2.zip").read().use {
//                        it.copyTo(resp.complete(), buf)
//                    }
//                }
//            }
//        )
//
//        var done = false
//        server.bindHTTP(NetworkAddress.Immutable(port = 9090))
//
//        while (true) {
//            nd.select()
//        }
//    }
}