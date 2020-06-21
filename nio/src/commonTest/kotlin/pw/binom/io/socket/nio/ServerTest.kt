package pw.binom.io.socket.nio

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.socket.RawSocketChannel
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.SocketSelector
import pw.binom.io.socket.rawSocketFactory
import pw.binom.io.use
import pw.binom.nextBytes
import pw.binom.printStacktrace
import pw.binom.thread.Runnable
import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.*

@OptIn(ExperimentalTime::class)
class ServerTest {
    val port = Random.nextInt(1000, 0xFFFF)
    val data = ByteDataBuffer.alloc(1024 * 1024).also {
        Random.nextBytes(it)
    }
    val readed = ByteDataBuffer.alloc(data.size)


    val totalRunTimer = Thread(Runnable {
        val startTime = TimeSource.Monotonic.markNow()
        val max = 5.0.toDuration(DurationUnit.SECONDS)
        while (true) {
            val v = startTime.elapsedNow()
            println("time: $v")
            if (!isExecuting()) {
                println("!")
                break
            }
            if (v > max) {
                break
            }
            Thread.sleep(1000)
        }
        if (isExecuting()) {
            println("Stop Test")
            done.value = true
        }
    })

    var done = AtomicBoolean(false)
    var good1 = AtomicBoolean(false)
    var good2 = AtomicBoolean(false)
    val clientThread = Thread(Runnable {
        try {
            val selector = SocketSelector(10)
            val client = SocketFactory.rawSocketFactory.createSocketChannel()
            var readTime = Duration.ZERO
            var writeTime = Duration.ZERO
            client.connect("127.0.0.1", port)
            client.blocking = false
            selector.reg(client).updateListening(true, false)
            var cursor = 0
            println("Try to read data from server")
            var write = false
            while (isExecuting()) {
//            println("Selector Process!")
                selector.process(1000) {
                    if (it.isReadable) {
                        readTime += measureTime {
                            println("Read!")
                            val channel = it.channel as RawSocketChannel
                            val r = channel.read(readed, cursor/*, length = minOf(readed.size - cursor, 1024 * 8)*/)
                            println("Readed $r")
                            cursor += r
                        }
                    }
                    if (it.isWritable) {
                        writeTime += measureTime {
                            println("Write!")
                            val channel = it.channel as RawSocketChannel
                            val r = channel.write(readed, cursor/*, length = minOf(readed.size - cursor, 1024 * 8)*/)
                            println("Wrote $r")
                            cursor += r
                        }
                    }
                    if (cursor == readed.size) {
                        if (write) {
                            println("Client is Done! - OK")
                            good1.value = true
                            return@process
                        }
                        println("Swap mode: Read -> Write")
                        cursor = 0
                        write = true
                        it.updateListening(false, true)
                    }
                }
            }
            println("Full ReadTime: $readTime")
        } catch (e: Throwable) {
            e.printStacktrace()
        }
    })

    val handler = object : SocketNIOManager.ConnectHandler {
        override fun clientConnected(connection: SocketNIOManager.ConnectionRaw, manager: SocketNIOManager) {
            connection { con ->
                try {
                    println("SERVER-Client connected! Send data...")
                    assertEquals(data.size, con.write(data))
                    println("SERVER-Data Sendded!")
                    println("SERVER-Try to read data from client")
                    ByteDataBuffer.alloc(data.size).use { tmp ->
                        con.readFull2(tmp)
                        data.forEachIndexed { index, byte ->
                            assertEquals(byte, tmp[index])
                        }
                    }
                    println("SERVER-Data Readed")
                    good2.value = true
                } catch (e: Throwable) {
                    println("ERROR $e")
                    throw e
                }
            }
        }
    }

    fun isExecuting(): Boolean {
        return when {
            !good1.value || !good2.value -> true
            done.value -> true
            else -> false
        }
//        (!good1.value || good2.value) && !done.value
    }

    suspend fun AsyncInput.readFull2(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int {
        var off = offset
        var len = length
        while (len > 0) {
            print("MAIN-($this)-Try to read $len...")
            val t = read(data, off, len)
            println("OK: $t")
            off += t
            len -= t
        }
        return length
    }

    @Test
    fun test() {

        val manager = SingleThreadNioManager()

        manager.bind("127.0.0.1", port = port, handler = handler)
        clientThread.start()
        totalRunTimer.start()
        while (isExecuting()) {
            println("Call update.. ${isExecuting()}  ${done.value}")
            manager.update(1000)
        }

        if (!good1.value || !good2.value)
            fail("Test Timeout")

        val assertTime = measureTime {
            data.forEachIndexed { index, byte ->
                assertEquals(byte, readed[index])
            }
        }
        println("Assert time: $assertTime")
    }
}