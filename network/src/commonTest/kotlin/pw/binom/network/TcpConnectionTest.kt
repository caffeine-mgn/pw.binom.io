package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.Worker
import pw.binom.concurrency.asReference
import pw.binom.concurrency.useReference
import pw.binom.getOrException
import pw.binom.readByte
import pw.binom.writeByte
import kotlin.native.concurrent.SharedImmutable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SharedImmutable
private var done1 by AtomicBoolean(false)
private var done2 by AtomicBoolean(false)

class TcpConnectionTest {
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