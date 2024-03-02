package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.PEM_write_bio_RSA_PUBKEY
import platform.openssl.RSA_free
import platform.openssl.RSA_new
import platform.openssl.RSA_set0_key
import pw.binom.base64.Base64
import pw.binom.checkTrue
import pw.binom.io.use
import pw.binom.ssl.Bio
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.throwError
import pw.binom.toBigNum

@OptIn(ExperimentalForeignApi::class)
actual class RSAPublicKey(
  actual val e: BigInteger,
  actual val n: BigInteger,
) : Key.Public {
  override val algorithm: KeyAlgorithm
    get() = KeyAlgorithm.RSA
  override val data: ByteArray
    get() {
      val rsa = RSA_new() ?: throwError("RSA_new fail")
      val nNum = n.toBigNum()
      val eNum = e.toBigNum()
      RSA_set0_key(
        r = rsa,
        n = nNum.ptr,
        e = eNum.ptr,
        d = null,
      ).checkTrue("RSA_set0_key fail") {
        nNum.free()
        eNum.free()
      }
      return Bio.mem().use { b ->
        PEM_write_bio_RSA_PUBKEY(b.self, rsa).checkTrue("PEM_write_bio_RSA_PUBKEY fail") {
          RSA_free(rsa)
          nNum.free()
          eNum.free()
        }
        RSA_free(rsa)
        val data = b.toByteArray()
        val str =
          data.decodeToString().replace("\n", "").removePrefix("-----BEGIN PUBLIC KEY-----")
            .removeSuffix("-----END PUBLIC KEY-----")
        Base64.decode(str)
      }
    }
  override val format: String
    get() = "X.509"
}
