package pw.binom.mq.nats.client

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.seconds

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

    @OptIn(ExperimentalTime::class)
    @Test
    fun echoTest() {
        val msg1 = "msg1"//Random.uuid().toString()
        val msg2 = "msg2"//Random.uuid().toString()
        var msg1ForCon1 = 0
        var msg2ForCon1 = 0
        var msg1ForCon2 = 0
        var msg2ForCon2 = 0
        val nd = NetworkDispatcher()
        val connector1 = NatsConnector(
            clientName = "Binom Client",
            user = null,
            pass = null,
            dispatcher = nd,
            tlsRequired = false,
            echo = false,
        ) {
            val data = it.data.toByteArray().decodeToString()
            if (data == msg1) {
                msg1ForCon1++
                return@NatsConnector
            }
            if (data == msg2) {
                msg2ForCon1++
                return@NatsConnector
            }
            throw IllegalStateException()
        }

        val connector2 = NatsConnector(
            clientName = "Binom Client",
            user = null,
            pass = null,
            dispatcher = nd,
            tlsRequired = false,
            echo = true,
        ) {
            val data = it.data.toByteArray().decodeToString()
            if (data == msg1) {
                msg1ForCon2++
                return@NatsConnector
            }
            if (data == msg2) {
                msg2ForCon2++
                return@NatsConnector
            }
            throw IllegalStateException()
        }

        val done = async2 {
            val subject = Random.uuid().toShortString()
            connector1.connect(NetworkAddress.Immutable("127.0.0.1", 4222))
            connector2.connect(NetworkAddress.Immutable("127.0.0.1", 4222))
            val t1Sub2 = connector2.subscribe(subject)
            val t1Sub1 = connector1.subscribe(subject)
            connector1.publish(subject = subject, data = msg1.encodeToByteArray())
            connector1.publish(subject = subject, data = msg2.encodeToByteArray())
//            connector1.unsubscribe(t1Sub)
//            println("try assert")
//            assertEquals(0, msg1ForCon1)
//            assertEquals(1, msg1ForCon2)
//            println("assert done")
        }
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            if (msg1ForCon1 == 0 && msg1ForCon2 == 1 && msg2ForCon2 == 1) {
                break
            }
            if (now.elapsedNow() > 5.0.seconds) {
                throw RuntimeException("Timeout")
            }
            nd.select(500)
        }
        if (done.isFailure) {
            throw done.exceptionOrNull!!
        }
    }
}