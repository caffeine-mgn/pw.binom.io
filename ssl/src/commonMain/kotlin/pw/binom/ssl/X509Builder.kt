package pw.binom.ssl

import pw.binom.date.DateTime

class X509Builder(
    val pair: KeyGenerator.KeyPair,
    val sign: PrivateKey = pair.createPrivateKey(),
    val version: Version = Version.V3,
    val subject: String,
    val issuer: String,
    val notBefore: DateTime,
    val notAfter: DateTime,
    val serialNumber: Long = 0L
) {
    enum class Version {
        V1, V3
    }
}

expect fun X509Builder.generate(): X509Certificate
