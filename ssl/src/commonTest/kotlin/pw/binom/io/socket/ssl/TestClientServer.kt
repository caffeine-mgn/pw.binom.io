package pw.binom.io.socket.ssl
/*
import pw.binom.Date
import pw.binom.Thread
import pw.binom.from
import pw.binom.io.readln
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.ssl.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestClientServer {

    class SimpleKeyManager(val private: PrivateKey?, val public: X509Certificate?) : KeyManager {
        override fun getPrivate(serverName: String?): PrivateKey? = private

        override fun getPublic(serverName: String?): X509Certificate? = public

        override fun close() {
            public?.close()
            private?.close()
        }

    }

    class EchoHandler : ConnectionManager.ConnectHandler {
        override fun clientConnected(connection: ConnectionManager.ConnectionRaw, manager: ConnectionManager) {
            connection {
                val reader = it.input.utf8Reader()
                val writer = it.output.utf8Appendable()
                val line = reader.readln()
                println("OLOLO! $line")
                writer.append("Echo $line\r\n")
            }
        }

    }

    class ServerTask : Task() {
        override fun execute() {

            val pairRoot = KeyGenerator.generate(
                    KeyAlgorithm.RSA,
                    2048
            )

            val pair1 = KeyGenerator.generate(
                    KeyAlgorithm.RSA,
                    2048
            )
            val private1 = pair1.createPrivateKey()
            val public2 = X509Builder(
                    pair = pair1,
                    notBefore = Date.now(),
                    notAfter = Date.now().apply { Date.from(year + 1, month, dayOfMonth, hours, min, sec) },
                    serialNumber = 10,
                    issuer = "DC=localhost",
                    subject = "CN=localhost",
                    sign = pairRoot.createPrivateKey()
            ).generate()


            val keyManager = SimpleKeyManager(private1, public2)
            SSLContext.getInstance(SSLMethod.TLS, keyManager, TrustManager.TRUST_ALL).use {
                val manager = ConnectionManager()
                manager.bind(host = "127.0.0.1", port = 3364, factory = it.socketFactory, handler = EchoHandler())

                while (!isInterrupted) {
                    manager.update(100)
                }
            }
        }

    }

    @Test
    fun test() {


        val server = Worker().execute { ServerTask() }

        val manager = ConnectionManager()
        var done = true

        SSLContext.getInstance(SSLMethod.TLS, SimpleKeyManager(null, null), TrustManager.TRUST_ALL).use {
            val con = manager.connect("127.0.0.1", 3364, factory = it.socketFactory)
            con {
                val writer = it.output.utf8Appendable()
                val reader = it.input.utf8Reader()
                val txt = "Hello from client"
//                println("Write text")
                writer.append("$txt\r\n")
                assertEquals("Echo $txt", reader.readln())
                done = true
            }

            val tt = Thread.currentTimeMillis()
            while (true) {
                if (done)
                    break
                if (Thread.currentTimeMillis() - tt > 10_000)
                    break
                manager.update(100)
            }

            server.interrupt()
        }


        /*
println("Start test!")
val txt = "Hello from TLS"
var done = false
SSLContext.getInstance(SSLMethod.TLSv1_2).use {
    val factory = it.socketFactory
    val handler = object : ConnectionManager.ConnectHandler {
        override fun clientConnected(connection: ConnectionManager.Connection, manager: ConnectionManager) {
            println("Connected")
            connection {
                it.output.utf8Appendable().append("$txt\r\n")
            }
        }

    }
    val manager = ConnectionManager()
    val port = 8924
    println("Bind")
    manager.bind(host = "127.0.0.1", port = port, factory = factory, handler = handler)
    println("Connect...")
    manager.connect("127.0.0.1", port, factory).invoke {
        assertEquals(txt, it.input.utf8Reader().readln())
        done = true
    }


    val start = Thread.currentTimeMillis()
    while (!done) {
        if (Thread.currentTimeMillis() - start > 5000)
            break
        manager.update(1000)
    }
}
*/
    }
}
*/