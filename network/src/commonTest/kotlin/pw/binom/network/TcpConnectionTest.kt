package pw.binom.network

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.*
import kotlin.native.concurrent.SharedImmutable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class TcpConnectionTest {

    @Test
    fun writeErrorTest() {
        val nd = NetworkDispatcher()
        val port = Random.nextInt(1000, Short.MAX_VALUE - 100)
        val address = NetworkAddress.Immutable("127.0.0.1", port)
        val worker = Worker()
        val spinLock = SpinLock()
        val r = nd.async {
            val server = nd.bindTcp(address)
            Worker.sleep(500)
            val client = nd.tcpConnect(address)

            nd.async {
                try {
                    spinLock.synchronize {
                        println("Try read...")
                        val readed = ByteBuffer.alloc(5) {
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

            val remoteClient = server.accept()!!
            server.close()
            ByteBuffer.alloc(10) { buf ->
                remoteClient.write(buf)
                execute(worker) {
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

        while (!r.isDone) {
            nd.select(100)
        }
        r.getOrException()
    }

    @Test
    fun waitWriteTest() {
        val nd = NetworkDispatcher()
        val port = Random.nextInt(1000, Short.MAX_VALUE - 100)

        val worker = Worker()

        val r = nd.async {
            val server = nd.bindTcp(NetworkAddress.Immutable(host = "127.0.0.1", port = port))

            val newClient = server.accept()!!
            newClient.useReference { ref ->
                execute(worker) {
                    println("net thread...   ${ref.owner.same}")
                    assertFalse(ref.owner.same)
                    println("Wait send...")
                    network {
                        println("Wait net thread...")
                        val buf = ByteBuffer.alloc(10)
                        val connection = ref.value
                        connection.writeByte(buf, 42)
                        connection.flush()
                        println("wroted!")
                    }
                }
            }
        }

        val r2 = nd.async {
            val client = nd.tcpConnect(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
            val b = ByteBuffer.alloc(10)
            println("Reading...")
            assertEquals(42, client.readByte(b))
            println("Read")
        }
        while (!r.isDone || !r2.isDone) {
            nd.select(100)
        }
        r.getOrException()
        r2.getOrException()
    }
}