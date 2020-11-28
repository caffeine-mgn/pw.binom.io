package pw.binom.ssl

import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import platform.openssl.*
import pw.binom.date.Date

actual fun X509Builder.generate(): X509Certificate {
    val x = X509_new()!!

    val ver = when (version) {
        X509Builder.Version.V1 -> 0
        X509Builder.Version.V3 -> 2
    }
    X509_set_version(x, ver.convert())
    ASN1_INTEGER_set_int64(X509_get_serialNumber(x), serialNumber)
    X509_gmtime_adj(X509_get_notBefore!!.invoke(x), ((Date.now - notBefore.time) / 1000).convert())
    X509_gmtime_adj(X509_get_notAfter!!.invoke(x), ((Date.now - notAfter.time) / 1000).convert())
    X509_set_pubkey(x, pair!!.native)

    X509_get_subject_name(x)!!.addEntry(subject!!)
    X509_get_issuer_name(x)!!.addEntry(issuer!!)

    if (X509_sign(x, sign!!.native, EVP_sha1()) <= 0)
        TODO("X509_sign error")

    return X509Certificate(x)
}