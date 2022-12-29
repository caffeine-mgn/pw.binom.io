package pw.binom.dns

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import pw.binom.dns.protocol.DnsHeader
import pw.binom.dns.protocol.QueryPackage
import pw.binom.dns.protocol.ResourcePackage
import pw.binom.io.ByteBuffer
import pw.binom.io.bufferedInput
import pw.binom.io.bufferedOutput
import pw.binom.network.NetworkAddressOld
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.network.tcpConnect
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class ClientTest {

    @Ignore
    @OptIn(ExperimentalTime::class)
    @Test
    fun test() = runTest {
        val n = NetworkCoroutineDispatcherImpl()

        val header = DnsHeader().apply {
            id = Random.nextInt().toShort()
            qr = false
            opcode = 0
            aa = false
            tc = false
            rd = true
            ra = false
            z = false
            ad = true
            cd = false
            rcode = 0
            q_count = 1u
            ans_count = 0u
            auth_count = 0u
            add_count = 1u
        }
        val query = QueryPackage().apply {
            name = "google.com"
            type = 1u
            clazz = 1u
        }
        val r = ResourcePackage().apply {
            name = ""
            type = 41u
            clazz = 4096u
            ttl = 0u
            rdata = ubyteArrayOf(
                0x0u,
                0xau,
                0x0u,
                0x8u,
                0xa3u,
                0x12u,
                0x9eu,
                0xe0u,
                0xa3u,
                0x4bu,
                0xc8u,
                0xfeu
            ).toByteArray()
        }
        val record = DnsRecord(
            id = Random.nextInt().toShort(),
            qr = false,
            opcode = 0,
            aa = false,
            tc = false,
            rd = true,
            ra = false,
            z = false,
            ad = true,
            cd = false,
            rcode = 0,
            queries = listOf(
                Query(
                    name = "google.com",
                    type = 1u,
                    clazz = 1u,
                )
            ),
            add = listOf(
                Resource(
                    name = "",
                    type = 41u,
                    clazz = 4096u,
                    ttl = 0u,
                    rdata = ubyteArrayOf(
                        0x0u,
                        0xau,
                        0x0u,
                        0x8u,
                        0xa3u,
                        0x12u,
                        0x9eu,
                        0xe0u,
                        0xa3u,
                        0x4bu,
                        0xc8u,
                        0xfeu
                    ).toByteArray()
                )
            ),
            ans = emptyList(),
            auth = emptyList()
        )
        val buf = ByteBuffer.alloc(512)
        try {
            val con = n.tcpConnect(NetworkAddressOld.Immutable("8.8.8.8", 53))
            val output = con.bufferedOutput()
            val input = con.bufferedInput()

            val w = record.write(output, buf)
            println("ww: $w")
            output.flush()

//                val startPos = header.writeStart(buf)
//                query.write(buf)
//                r.write(buf)
//                header.writeEnd(startPos, buf)
//                output.write(buf)
//                output.flush()
            header.read(input, buf)

            repeat(header.q_count.toInt()) {
                query.read(buf)
                println("Q->$query")
            }
            repeat(header.ans_count.toInt()) {
                r.read(buf)
                println("ANS->$r")
            }
            repeat(header.auth_count.toInt()) {
                r.read(buf)
                println("AUTH->$r")
            }
            repeat(header.add_count.toInt()) {
                r.read(buf)
                println("ADD->$r")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    @OptIn(ExperimentalTime::class)
    @Ignore
    @Test
    fun serverTest() = runTest {
        val n = NetworkCoroutineDispatcherImpl()
        val buf = ByteBuffer.alloc(512)
        val header = DnsHeader()
        val q = QueryPackage()
        val r = ResourcePackage()
        try {
            val server = n.bindTcp(NetworkAddressOld.Immutable("0.0.0.0", 53))
            while (true) {
                val client = server.accept()

                GlobalScope.launch {
                    println("Client connected!")
                    try {
                        println("Reading header...")
                        header.read(client, buf)
                        println("Header: $header")
//                            header.read(client, buf)
// //                            println("Header [${header.q_count}]: $header")
                        repeat(header.q_count.toInt()) {
                            println("Reading Q...")
                            q.read(buf)
                            println("->$q")
                        }
                        repeat(header.add_count.toInt()) {
                            println("Read Add...")
                            r.read(buf)
                            println("Add $r")
                        }
                    } catch (e: Throwable) {
                        println("ERROR!")
                        e.printStackTrace()
                    } finally {
                        client.closeAnyway()
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}
