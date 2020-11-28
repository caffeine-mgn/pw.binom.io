package pw.binom.ssl

import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SNIHostName
import javax.net.ssl.SNIServerName
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager

class BinomX509KeyManager(val keyManager:KeyManager/*, val private: (serverName: String?) -> PrivateKey?, val public: (serverName: String?) -> X509Certificate?*/) : X509ExtendedKeyManager() {

    override fun chooseEngineClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, engine: SSLEngine?): String? {
        return "private"
        return super.chooseEngineClientAlias(keyType, issuers, engine)
    }

    override fun chooseEngineServerAlias(keyType: String?, issuers: Array<out Principal>?, engine: SSLEngine?): String? {
        if (keyType != "RSA")
            return null

        var serverName: String? = null
        if (engine != null) {
            val f = engine.handshakeSession::class.java.getDeclaredField("requestedServerNames")
            f.isAccessible = true
            val value = f.get(engine.handshakeSession) as List<SNIServerName>
            val element = value.find { it is SNIHostName } as SNIHostName?
            if (element != null)
                serverName = element.asciiName
        }
        return "server:${serverName ?: ""}"
    }

    override fun getClientAliases(p0: String?, p1: Array<out Principal>?): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServerAliases(p0: String?, p1: Array<out Principal>?): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun chooseServerAlias(p0: String?, p1: Array<out Principal>?, p2: Socket?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCertificateChain(p0: String): Array<X509Certificate>? {
        val items = p0.split(':', limit = 2)
        val server = items[0] == "server"
        val cer = keyManager.getPublic(items[1].takeIf { it.isNotBlank() })?.native?:return null
//        val cer = public(items[1].takeIf { it.isNotBlank() }) ?: return null
        return arrayOf(cer)
    }

    override fun getPrivateKey(p0: String): PrivateKey? {
        val items = p0.split(':', limit = 2)
        val server = items[0] == "server"
        return keyManager.getPrivate(items[1].takeIf { it.isNotBlank() })?.native
//        return private(items[1].takeIf { it.isNotBlank() })
    }

    override fun chooseClientAlias(p0: Array<out String>?, p1: Array<out Principal>?, p2: Socket?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}