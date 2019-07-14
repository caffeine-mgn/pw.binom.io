package pw.binom.ssl

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.io.ByteArrayOutputStream

// Documentation https://stackoverflow.com/questions/18155559/how-does-one-access-the-raw-ecdh-public-key-private-key-and-params-inside-opens
class PrivateKeyImpl(override val algorithm: KeyAlgorithm, override val native: CPointer<EVP_PKEY>) : PrivateKey {
    override val data: ByteArray
        get() {

            when (algorithm){
                KeyAlgorithm.RSA->{
                    val rsa = EVP_PKEY_get1_RSA(native)?:TODO("EVP_PKEY_get1_RSA returns null")
                    val b = Bio.mem()
                    i2d_RSAPrivateKey_bio(b.self,rsa)
                    val o = ByteArrayOutputStream()
                    b.copyTo(o)
                    b.close()
                    return o.toByteArray()
                }
            }

            /*
            EVP_PKEY_get_raw_private_key()
            val ec = EVP_PKEY_get1_EC_KEY(native)?:TODO("EVP_PKEY_get1_EC_KEY null")
            val bigNum = EC_KEY_get0_private_key(ec)?:TODO("EC_KEY_get0_private_key null")
            EC_KEY_free(ec)
            val size = BN_bn2mpi(bigNum, null)

            val b = UByteArray(size)
            val bb = b.refTo(0)//allocArray<UByteVar>(size)
            BN_bn2mpi(bigNum, bb)
            BN_free(bigNum)

            return b.toByteArray()
            */
        }

    override fun close() {
        EVP_PKEY_free(native)
    }
}

actual fun PrivateKey.Companion.loadRSA(data: ByteArray): PrivateKey {

    val b = Bio.mem(data)
    val rsa = d2i_RSAPrivateKey_bio(b.self,null)
    val k = EVP_PKEY_new()!!
    EVP_PKEY_set1_RSA(k,rsa)
    return PrivateKeyImpl(algorithm = KeyAlgorithm.RSA, native = k)

//    val u = data.toUByteArray()
//    val bigNum = BN_mpi2bn(u.refTo(0), u.size, null)
//    val ec = EC_KEY_new()
//    EC_KEY_set_private_key(ec, bigNum)
//    BN_free(bigNum)
//
//    val key = EVP_PKEY_new()!!
//    EVP_PKEY_set1_EC_KEY(key, ec)
//    EC_KEY_free(ec)
//    return PrivateKeyImpl(algorithm = KeyAlgorithm.RSA, native = key)
}