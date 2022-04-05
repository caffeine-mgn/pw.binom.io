package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import platform.posix.memcpy

fun CPointer<X509_NAME>.addEntry(name:String,value:String){
    memScoped {
        val value1 = allocArray<UByteVar>(value.length + 1)
        memcpy(value1, value.cstr, (value.length + 1).convert())
        value1[value.length] = 0.toUByte()
        X509_NAME_add_entry_by_txt(this@addEntry, name, MBSTRING_ASC, value1, -1, -1, 0)
    }
}

fun CPointer<X509_NAME>.addEntry(text:String){
    val items = text.split("=",limit = 2)
    addEntry(items[0], items[1])
}

/*
actual object CertificateGenerator {
    actual fun generate(
            keySize: Int,
            algorithm: CertificateAlgorithm,
            from: Date,
            to: Date,
            principals: Map<String, String>,
            serialNumber: Long,
            subject:String?,
            issuer:String?): Pair<PrivateCertificate, X509Certificate> {
        val pk = EVP_PKEY_new()!!
        val x = X509_new()!!
        val rsa = RSA_generate_key(keySize, RSA_F4.convert(), null, null)
        if (EVP_PKEY_assign(pk, EVP_PKEY_RSA, rsa) <= 0)
            TODO("EVP_PKEY_assign error")

        X509_set_version(x, 2)
        ASN1_INTEGER_set_int64(X509_get_serialNumber(x), serialNumber)
        X509_gmtime_adj(X509_get_notBefore!!.invoke(x), ((Date.now().time - from.time) / 1000).convert())
        X509_gmtime_adj(X509_get_notAfter!!.invoke(x), ((Date.now().time - to.time) / 1000).convert())
        X509_set_pubkey(x, pk)

        if (subject!=null) {
            X509_get_subject_name(x)!!.addEntry(subject)
        }
        if (issuer!=null) {
            X509_get_issuer_name(x)!!.addEntry(issuer)
        }

        if (X509_sign(x, pk, EVP_sha1())<=0)
            TODO("X509_sign error")

        return PrivateCertificate(pk) to X509Certificate(x)
    }
}
*/
