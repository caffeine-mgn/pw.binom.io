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
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.network.tcpConnect
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class ClientTest {

    //    @Ignore
    @OptIn(ExperimentalTime::class)
    @Test
    fun test() = runTest {
        val n = NetworkCoroutineDispatcherImpl()

        val header = DnsHeader().apply {
            header.id = Random.nextInt().toShort()
            header.qr = false
            header.opcode = Opcode.QUERY
            header.aa = false
            header.tc = false
            header.rd = true
            header.ra = false
            header.z = false
            header.ad = true
            header.cd = false
            header.rcode = Rcode.NOERROR
            qCount = 1u
            ansCount = 0u
            authCount = 0u
            addCount = 1u
        }
        val query = QueryPackage().apply {
            name = "google.com"
            type = Type.A
            clazz = Class.IN
        }
        val r = ResourcePackage().apply {
            name = ""
            type = Type.OPT
            clazz = Class(4096u)
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
                0xfeu,
            ).toByteArray()
        }
        val record = DnsRecord(
            header = Header(
                id = Random.nextInt().toShort(),
                qr = false,
                opcode = Opcode.QUERY,
                aa = false,
                tc = false,
                rd = true,
                ra = false,
                z = false,
                ad = true,
                cd = false,
                rcode = Rcode.NOERROR,
            ),
            queries = listOf(
                Query(
                    name = "google.com",
                    type = Type.A,
                    clazz = Class.IN,
                ),
            ),
            add = listOf(
                Resource(
                    name = "",
                    type = Type.OPT,
                    clazz = Class(4096u),
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
                        0xfeu,
                    ).toByteArray(),
                ),
            ),
            ans = emptyList(),
            auth = emptyList(),
        )
        val buf = ByteBuffer(512)
        try {
            val con = n.tcpConnect(NetworkAddress.create("8.8.8.8", 53))
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

            repeat(header.qCount.toInt()) {
                query.read(buf)
                println("Q->$query")
            }
            repeat(header.ansCount.toInt()) {
                r.read(buf)
                println("ANS->$r")
            }
            repeat(header.authCount.toInt()) {
                r.read(buf)
                println("AUTH->$r")
            }
            repeat(header.addCount.toInt()) {
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
        val buf = ByteBuffer(512)
        val header = DnsHeader()
        val q = QueryPackage()
        val r = ResourcePackage()
        try {
            val server = n.bindTcp(NetworkAddress.create("0.0.0.0", 53))
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
                        repeat(header.qCount.toInt()) {
                            println("Reading Q...")
                            q.read(buf)
                            println("->$q")
                        }
                        repeat(header.addCount.toInt()) {
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
