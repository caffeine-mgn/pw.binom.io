package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.BigNum
import pw.binom.base64.Base64
import pw.binom.checkTrue
import pw.binom.getSslError
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.ssl.Bio
import pw.binom.ssl.ECKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.throwError
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ECPrivateKey(val native: CPointer<EC_KEY>) : Key.Private, ECKey {
  actual override val algorithm: KeyAlgorithm
    get() = KeyAlgorithm.ECDSA

  private val cleaner =
    createCleaner(native) { native ->
      EC_KEY_free(native)
    }

  actual val d: BigInteger
    get() {
      val num = EC_KEY_get0_private_key(native) ?: throwError("EC_KEY_get0_private_key fails")
      return BigNum(num).toBigInt()
    }

  actual override val data: ByteArray
    get() =
      Bio.mem().use { mem ->
//            Bio.mem().use { m ->
//                PEM_write_bio_ECPrivateKey(
//                    bp = m.self,
//                    x = native,
//                    enc = null,
//                    kstr = null,
//                    klen = 0,
//                    cb = null,
//                    u = null,
//                ).checkTrue("PEM_write_bio_ECPrivateKey fails")
//                val str = m.toByteArray().decodeToString() // .replace("\n", "")
//                println("===============================")
//                println(str)
//                println("===============================")
//            }
        val p = EVP_PKEY_new() ?: throwError("EVP_PKEY_new fails")
        EVP_PKEY_set1_EC_KEY(p, EC_KEY_dup(native) ?: throwError("EC_KEY_dup fails"))
        PEM_write_bio_PKCS8PrivateKey(
          mem.self,
          p,
          null,
          null,
          0,
          null,
          null,
        ).checkTrue("PEM_write_bio_PKCS8PrivateKey fails")
        val c = mem.toByteArray()
        var str = c.decodeToString()
        str = str.replace("\n", "")
        if (!str.startsWith("-----BEGIN PRIVATE KEY-----")) {
          TODO()
        }
        if (!str.endsWith("-----END PRIVATE KEY-----")) {
          TODO()
        }
        str = str.substring(27, str.length - 25)
        Base64.decode(str)
      }
  actual override val format: String
    get() = "PKCS#8"

  actual companion object {
    actual fun load(data: ByteArray): ECPrivateKey {
      val pem = "-----BEGIN PRIVATE KEY-----\n${Base64.encode(data)}\n-----END PRIVATE KEY-----\n"
      val ecKey =
        Bio.mem(pem.encodeToByteArray()).use { priv ->
          PEM_read_bio_ECPrivateKey(priv.self, null, null, null)
            ?: throw IOException("Can't load private key: ${getSslError()}")
        }
      EC_KEY_check_key(ecKey).checkTrue("EC_KEY_check_key") {
        EC_KEY_free(ecKey)
      }
      return ECPrivateKey(ecKey)
    }
  }
}
