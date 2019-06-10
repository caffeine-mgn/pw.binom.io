package pw.binom.ssl

import pw.binom.io.Closeable

interface KeyManager:Closeable {
    fun getPrivate(serverName: String?): PrivateKey?
    fun getPublic(serverName: String?): X509Certificate?
}