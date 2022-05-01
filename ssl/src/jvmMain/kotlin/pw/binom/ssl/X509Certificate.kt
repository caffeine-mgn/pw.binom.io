package pw.binom.ssl

import pw.binom.io.Closeable
import java.io.ByteArrayInputStream

actual class X509Certificate(val native: java.security.cert.X509Certificate) : Closeable {
    override fun close() {
    }

    actual companion object {

        actual fun load(data: ByteArray): X509Certificate {
            val factory = java.security.cert.CertificateFactory.getInstance("X.509")
            val cer = factory.generateCertificate(ByteArrayInputStream(data)) as java.security.cert.X509Certificate
            return X509Certificate(cer)
        }
    }

    actual fun save(): ByteArray = native.encoded
}

// fun InputStream.toJava()=object :java.io.InputStream(){
//    override fun read(): Int =this@toJava.read().toInt()
//
//    override fun available(): Int =this@toJava.available
//
//    override fun close() {
//        this@toJava.close()
//    }
//
//    override fun read(b: ByteArray, off: Int, len: Int): Int =this@toJava.read(b,off,len)
//
// }
