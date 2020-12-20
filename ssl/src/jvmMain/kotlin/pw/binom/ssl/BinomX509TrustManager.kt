package pw.binom.ssl

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class BinomX509TrustManager(val trustManager: TrustManager) : X509TrustManager {

    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val aa = ArrayList<X509Certificate>()

    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        val l = p0?.let { Array(it.size) {
            X509Certificate(p0[it])
        }}

        if (!trustManager.isServerTrusted(l)) {
            throw CertificateException("Untrusted Client Certificate Chain")
        }
        p0?.let {
            aa.addAll(it)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = aa.toTypedArray()

}