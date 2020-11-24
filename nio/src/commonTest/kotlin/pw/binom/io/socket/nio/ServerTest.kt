package pw.binom.io.socket.nio

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.socket.RawSocketChannel
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.SocketSelector
import pw.binom.io.socket.rawSocketFactory
import pw.binom.io.use
import pw.binom.nextBytes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.*
//
//@OptIn(ExperimentalTime::class)
//class ServerTest {
//    val port = Random.nextInt(1000, 0xFFFF)
//    val data = ByteBuffer.alloc(1024 * 1024).also {
//        Random.nextBytes(it)
//        it.clear()
//    }
//    val readed = ByteBuffer.alloc(data.capacity)
//
//
//    val totalRunTimer = Thread(Runnable {
//        val startTime = TimeSource.Monotonic.markNow()
//        val max = 5.0.toDuration(DurationUnit.SECONDS)
//        while (true) {
//            val v = startTime.elapsedNow()
//            println("time: $v")
//            if (!isExecuting()) {
//                println("!")
//                break
//            }
//            if (v > max) {
//                break
//            }
//            Thread.sleep(1000)
//        }
//        if (isExecuting()) {
//            println("Stop Test")
//            done.value = true
//        }
//    })
//
//    var done = AtomicBoolean(false)
//    var good1 = AtomicBoolean(false)
//    var good2 = AtomicBoolean(false)
//    val clientThread = Thread(Runnable {
//        try {
//            val selector = SocketSelector()
//            val client = SocketFactory.rawSocketFactory.createSocketChannel()
//            client.connect("127.0.0.1", port)
//            client.blocking = false
//            selector.reg(client).listen(true, false)
//            println("Try to read data from server")
//            var write = false
//            while (isExecuting()) {
////            println("Selector Process!")
//                selector.process(1000) {
//                    if (it.isReadable) {
//                        if (readed.remaining > 0) {
//                            println("Read!")
//                            val channel = it.channel as RawSocketChannel
//                            val r = channel.read(readed/*, length = minOf(readed.size - cursor, 1024 * 8)*/)
//                            println("Readed $r. Need read: [${readed.remaining} bytes]")
//                            if (readed.remaining == 0) {
//                                println("Swap mode: Read -> Write")
//                                write = true
//                                readed.flip()
//                                it.listen(false, true)
//                            }
//                        }
//                    }
//                    if (it.isWritable) {
//                        if (readed.remaining > 0) {
//                            println("Write! Need write: [${readed.remaining} bytes]")
//                            val channel = it.channel as RawSocketChannel
//                            val r = channel.write(readed/*, length = minOf(readed.size - cursor, 1024 * 8)*/)
//                            println("Wrote $r")
//                            if (readed.remaining == 0) {
//                                println("Client is Done! - OK")
//                                good1.value = true
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (e: Throwable) {
//            e.printStackTrace()
//        }
//    })
//
//    val handler = object : SocketNIOManager.ConnectHandler {
//        override fun clientConnected(connection: TcpConnectionRaw, manager: SocketNIOManager) {
//            connection { con ->
//                try {
//                    println("SERVER-Client connected! Send data... size: [${data.remaining}]")
//                    assertEquals(data.capacity, con.write(data))
//                    println("SERVER-Data Sendded!")
//                    println("SERVER-Try to read data from client")
//                    ByteBuffer.alloc(data.capacity).use { tmp ->
//                        con.readFull2(tmp)
//                        assertArrayEquals(data, 0, tmp, 0, data.capacity)
//                    }
//                    println("SERVER-Data Readed")
//                    good2.value = true
//                } catch (e: Throwable) {
//                    println("ERROR $e")
//                    throw e
//                }
//            }
//        }
//    }
//
//    fun isExecuting(): Boolean {
//        return when {
//            good1.value && good2.value -> false
//            !done.value && (!good1.value || !good2.value) -> true
//            done.value -> false
//            !done.value -> true
//            else -> false
//        }
////        (!good1.value || good2.value) && !done.value
//    }
//
//    suspend fun AsyncInput.readFull2(data: ByteBuffer): Int {
//        var l = data.remaining
//        while (data.remaining > 0) {
//            print("MAIN-($this)-Try to read ${data.remaining}...")
//            val t = read(data)
//            println("OK: $t")
//        }
//        return l
//    }
//
//    @Test
//    fun test() {
//
//        val manager = SocketNIOManager()
//
//        manager.bind("127.0.0.1", port = port, handler = handler)
//        clientThread.start()
//        totalRunTimer.start()
//        while (isExecuting()) {
//            println("Call update... isExecuting: [${isExecuting()}], done: [${done.value}], good1: [${good1.value}], good2: [${good2.value}]")
//            manager.update(1000)
//        }
//
//        if (!good1.value || !good2.value)
//            fail("Test Timeout")
//
//        val assertTime = measureTime {
//            assertArrayEquals(data, 0, readed, 0, data.capacity)
//        }
//        println("Assert time: $assertTime")
//    }
//}
//
fun assertArrayEquals(expected: ByteBuffer, expectedOffset: Int, actual: ByteBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}