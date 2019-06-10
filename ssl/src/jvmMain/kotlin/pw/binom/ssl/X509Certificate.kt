package pw.binom.ssl

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import pw.binom.io.Closeable
import java.io.ByteArrayInputStream


actual class X509Certificate(val native: java.security.cert.X509Certificate) : Closeable {
    override fun close() {
    }

    actual companion object {

        actual fun load(data: ByteArray): X509Certificate =
                X509Certificate(CertificateFactory().engineGenerateCertificate(ByteArrayInputStream(data)) as java.security.cert.X509Certificate)
    }

    actual fun save(): ByteArray = native.encoded
}

//fun InputStream.toJava()=object :java.io.InputStream(){
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
//}