package pw.binom.dns

import pw.binom.*
import pw.binom.io.bufferedInput
import pw.binom.io.bufferedOutput
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.random.Random
import kotlin.test.Test

const val T_A = 1.toShort()

class ClientTest {

    @Test
    fun test() {
        val n = NetworkDispatcher()
        var done = false

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
        val query = Query().apply {
            name = "google.com"
            type = 1u
            clazz = 1u
        }
        val r = ResourceRecord().apply {
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
        val buf = ByteBuffer.alloc(512)
        async {
            try {
                val con = n.tcpConnect(NetworkAddress.Immutable("8.8.8.8", 53))
                val output = con.bufferedOutput()
                val input = con.bufferedInput()
                header.writeStart(buf)
                query.write(buf)
                r.write(buf)
                header.writeEnd(buf)
                output.write(buf)
                output.flush()
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
            } finally {
                done = true
            }
        }

        while (!done) {
            n.select(1000)
        }
    }

    @Test
    fun test2() {
        println("15=${Bitset32(15).toString()}")
        val n = NetworkDispatcher()
        var done = false
        val buf = ByteBuffer.alloc(512)
        val header = DnsHeader()
        val q = Query()
        val r = ResourceRecord()
        async {
            try {
                val server = n.bindTcp(NetworkAddress.Immutable("0.0.0.0", 53))
                while (true) {
                    val client = server.accept()

                    if (client != null)
                        async {
                            println("Client connected!")
                            try {
                                println("Reading header...")
                                header.read(client, buf)
                                println("Header: $header")
//                            header.read(client, buf)
////                            println("Header [${header.q_count}]: $header")
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
                                runCatching { client.close() }
                            }
                        }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                done = true
            }
        }
        while (!done) {
            n.select(1000)
        }
    }
}

inline val Short.hostToNetworkByteOrder
    get() = this

inline val Short.networkToHostByteOrder
    get() = this

inline val Int.htons
    get() = this

val UByte.hex: String
    get() {
        val v = toString(16)
        return if (v.length >= 2) v else "0$v"
    }