package pw.binom.io.socket.ssl

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pw.binom.date.Date
import pw.binom.io.ByteBuffer
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.network.bindTcp
import pw.binom.network.tcpConnect
import pw.binom.readLong
import pw.binom.ssl.*
import pw.binom.writeLong
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleKeyManager(val private: PrivateKey?, val public: X509Certificate?) : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? {
        return private
    }

    override fun getPublic(serverName: String?): X509Certificate? {
        return public
    }

    override fun close() {
        public?.close()
        private?.close()
    }
}

class SSLTest {

    @Test
    fun test() = runBlocking {

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
            notBefore = Date(),
            notAfter = Date(Date.nowTime + 1000 * 60 * 60),
            serialNumber = 10,
            issuer = "DC=localhost",
            subject = "CN=localhost",
            sign = pairRoot.createPrivateKey()
        ).generate()

        val context = SSLContext.getInstance(
            method = SSLMethod.TLS,
            keyManager = SimpleKeyManager(private1, public2),
            trustManager = TrustManager.TRUST_ALL,
        )
        val context2 = SSLContext.getInstance(
            method = SSLMethod.TLSv1_2,
            keyManager = SimpleKeyManager(private1, public2),
            trustManager = TrustManager.TRUST_ALL,
        )
        val buf = ByteBuffer.alloc(16)
        val nd = NetworkCoroutineDispatcherImpl()
        val addr = NetworkAddress.Immutable("127.0.0.1", 4445)
        val server = nd.bindTcp(addr)
        val r1 = launch {
            val client = server.accept()
            val clientSsl = AsyncSSLChannel(
                context2.serverSession(),
                client
            )
            clientSsl.writeLong(buf, 100500)
            clientSsl.flush()
        }

        val r2 = launch {
            val client = nd.tcpConnect(addr)
            val clientSsl = AsyncSSLChannel(
                context.clientSession(addr.host, addr.port),
                client
            )
            assertEquals(100500L, clientSsl.readLong(buf))
        }
        r1.join()
        r2.join()
    }
}
