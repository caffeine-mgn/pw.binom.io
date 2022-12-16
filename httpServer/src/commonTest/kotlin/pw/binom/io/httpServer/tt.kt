package pw.binom.io.httpServer

import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class tt {
    @Test
    fun runTest() {
        println("Start test")
        val manager = NetworkCoroutineDispatcherImpl()
        val server = HttpServer(
            manager = manager,
            handler = Handler {
                println("->${it.request}")
                println("->${it.headers}")
                println("Request income ${it.request}")
                println("Headers: ${it.headers}")
                it.response {
                    val myText = "0000000000000000000000".encodeToByteArray()
                    it.status = 200
                    it.headers.contentType = "text/html;charset=utf-8"
                    it.sendBinary(myText)
                    println("Request done!")
                }
            }
        )
        server.listenHttp(NetworkAddress.create("0.0.0.0", 50051))
        manager.networkThread.join()
    }
}

/*

@Ignore
class tt {

    class KeyManagerImpl : KeyManager {
        override fun close() {
            cers.values.forEach {
                it.first.close()
                it.second.close()
            }
        }

        init {
            neverFreeze()
        }

        private val cers = HashMap<String, Pair<PrivateKey, X509Certificate>>()

        private fun keyPair(host: String): Pair<PrivateKey, X509Certificate> {
            println("Request for host: $host")
            var r = cers[host]
            if (r != null)
                return r
            var public = File("$host.cer").takeIf { it.isFile }?.let {
                val b = ByteArrayOutputStream()
                FileInputStream(it).use {
                    it.copyTo(b)
                }
                X509Certificate.load(b.toByteArray())
            }

            var private = File("$host.pem").takeIf { it.isFile }?.let {
                //                val b = ByteArrayOutputStream()
                val obj = FileInputStream(it).use {
                    PemReader(it.utf8Reader()).use {
                        it.read()
                    }
                } ?: TODO("Can't load data from file $host.pem")
                if (obj.type == "RSA PRIVATE KEY")
                    PrivateKey.loadRSA(obj.date)
                else
                    TODO("Unknown private key type: ${obj.type}")
            }
            if (private != null && public != null) {
                r = private to public
                cers[host] = r
                return r
            }
            println("Generate cer for $host")
            val pair = KeyGenerator.generate(KeyAlgorithm.RSA, 2048)
            private = pair.createPrivateKey()

            public = X509Builder(
                    issuer = "DC=$host",
                    serialNumber = 10,
                    notAfter = Date(Date.now),
                    notBefore = Date(Date.now),
                    sign = private,
                    subject = "CN=$host",
                    pair = pair
            ).generate()
            pair.close()

            FileOutputStream(File("$host.cer")).use {
                it.write(public.save())
                it.flush()
            }

            FileOutputStream(File("$host.pem")).use {
                val w = PemWriter(it.utf8Appendable())
                w.write("RSA PRIVATE KEY", private.data)
                it.flush()
            }
            r = private to public
            cers[host] = r
            return r
        }

        override fun getPrivate(serverName: String?): PrivateKey? = keyPair(serverName ?: "default").first

        override fun getPublic(serverName: String?): X509Certificate? = keyPair(serverName ?: "default").second

    }

    @Ignore
    @Test
    fun test() {
        val manager = SocketNIOManager()

        class H : Handler {
            override suspend fun request(req: HttpRequest, resp: HttpResponse) {
                println("Main Thread ID: ${Thread.currentThread.id}")
                resp.status = 200
                val txt = "Hello from HTTPS"
                resp.resetHeader("Content-Length", txt.length.toString())
                resp.resetHeader("Connection", "keep-alive")
                resp.complete().utf8Appendable().append(txt)
            }
        }

        val b = HttpServer(manager, H())
//        b.bindHTTPS(SSLContext.getInstance(SSLMethod.TLS, KeyManagerImpl(), TrustManager.TRUST_ALL), port = 8899)

        val start = Thread.currentTimeMillis()
        while (true) {
            if (Thread.currentTimeMillis() - start > 5_000)
                break
            manager.update()
        }
    }
}*/
