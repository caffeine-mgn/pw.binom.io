package pw.binom.mq.nats.client

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test

class NatsConnectorTest {

    @Test
    fun test2() {
        val pool = ByteBufferPool(10)
        val subject = "T1"
        val replyTo: String? = null
        val data = ByteBuffer.wrap("Hello".encodeToByteArray())
        require(subject.isNotEmpty() && " " !in subject)
        require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))
        val out = ByteArrayOutput()
        val outAsync = out.asyncOutput()
        val app = outAsync.bufferedWriter(pool)
        async {
            app.append("PUB ").append(subject)
            if (replyTo != null) {
                app.append(" ").append(replyTo)
            }
            app.append(" ").append((data?.remaining ?: 0).toString()).append("\r\n")
            app.flush()


            if (data != null) {
                while (data.remaining > 0) {
                    outAsync.write(data)
                }
            }
            outAsync.flush()
            app.append("\r\n")
            app.flush()
            out.trimToSize()
            out.data.clear()
            out.data.forEachIndexed { index, value ->
                val c = when (val v = value.toChar()) {
                    '\r' -> "\\r"
                    '\n' -> "\\n"
                    else -> v.toString()
                }
                println("$index -> $c")
            }
        }
    }

    @Test
    fun test() {
        val nd = NetworkDispatcher()
        val connector1 = NatsConnector(
            clientName = "Binom Client",
            user = null,
            pass = null,
            dispatcher = nd,
            tlsRequired = false,
        ) {
            println(
                "connector1 => Message GOT! subject: [${it.subject}] sid: [${it.sid}], replyTo: [${it.replyTo}], data: [${
                    it.data.toByteArray().decodeToString()
                }]"
            )
        }

        val connector2 = NatsConnector(
            clientName = "Binom Client",
            user = null,
            pass = null,
            dispatcher = nd,
            tlsRequired = false,
        ) {
            println(
                "connector2 => Message GOT! subject: [${it.subject}] sid: [${it.sid}], replyTo: [${it.replyTo}], data: [${
                    it.data.toByteArray().decodeToString()
                }]"
            )
        }

        async {
            connector1.connect(NetworkAddress.Immutable("127.0.0.1", 4222))
            connector2.connect(NetworkAddress.Immutable("127.0.0.1", 4222))
            val t1Sub2 =     connector2.subscribe("T12")
            val t1Sub1 =     connector1.subscribe("T12")
            connector1.publish("T12", data = "Hello".encodeToByteArray())
//            connector1.unsubscribe(t1Sub)
        }

        while (true) {
            nd.select()
        }
    }
}