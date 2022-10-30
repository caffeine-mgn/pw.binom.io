package pw.binom.ssl

interface KeyManager {
    fun getPrivate(serverName: String?): PrivateKey?
    fun getPublic(serverName: String?): X509Certificate?
}
