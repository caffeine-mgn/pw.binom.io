package pw.binom.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pw.binom.*
import pw.binom.concurrency.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class TcpConnectionTest {

    @Test
    fun writeErrorTest() {
        val nd = NetworkCoroutineDispatcherImpl()
        val port = Random.nextInt(1000, Short.MAX_VALUE - 100)
        val address = NetworkAddress.Immutable("127.0.0.1", port)
        val worker = Worker()
        val spinLock = SpinLock()
        runBlocking {
            val server = nd.bindTcp(address)
            sleep(500)
            val client = nd.tcpConnect(address)
            launch {
                try {
                    spinLock.synchronize {
                        println("Try read...")
                        val readed = ByteBuffer.alloc(5) {
                            println("try read...")
                            client.read(it)
                        }
                        println("Reded $readed")
                        client.close()
                        println("Stop client!")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            println("Wait client connect")
            val remoteClient = server.accept()!!
            println("Client connected")
            server.close()
            ByteBuffer.alloc(10) { buf ->
                remoteClient.write(buf)
                withContext(worker) {
                    spinLock.lock()
                    spinLock.unlock()
                }

                try {
                    buf.clear()
                    remoteClient.write(buf)
                    remoteClient.flush()
                    fail()
                } catch (e: SocketClosedException) {
                    //ok
                }

                println("Done!")
                remoteClient.close()
            }
        }
    }

    @Test
    fun waitWriteTest() {
        val nd = NetworkCoroutineDispatcherImpl()
        val port = Random.nextInt(1000, Short.MAX_VALUE - 100)

        val worker = Worker()

        runBlocking {
            val r = launch {
                val server = nd.bindTcp(NetworkAddress.Immutable(host = "127.0.0.1", port = port))

                val newClient = server.accept()
                    withContext(worker) {
                        println("Wait send...")
                        launch {
                            println("Wait net thread...")
                            val buf = ByteBuffer.alloc(10)
                            newClient.writeByte(buf, 42)
                            newClient.flush()
                            println("wroted!")
                        }
                    }
                }

            val r2 = launch {
                val client = nd.tcpConnect(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
                val b = ByteBuffer.alloc(10)
                println("Reading...")
                assertEquals(42, client.readByte(b))
                println("Read")
            }
        }
    }
}