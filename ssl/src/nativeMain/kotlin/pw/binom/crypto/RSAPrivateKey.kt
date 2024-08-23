package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.*
import pw.binom.base64.Base64
import pw.binom.io.use
import pw.binom.security.SecurityException
import pw.binom.ssl.Bio
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class RSAPrivateKey(actual val n: BigInteger, actual val e: BigInteger, actual val d: BigInteger) : Key.Private {
  actual companion object {
    actual fun load(encodedKeySpec: KeySpec): RSAPrivateKey =
      when (encodedKeySpec) {
        is PKCS8EncodedKeySpec -> {
          val pem =
            "-----BEGIN RSA PRIVATE KEY-----\n${Base64.encode(encodedKeySpec.data)}\n-----END RSA PRIVATE KEY-----\n"
          val rsa =
            Bio.mem(pem.encodeToByteArray()).use { priv ->
              PEM_read_bio_RSAPrivateKey(priv.self, null, null, null)
                ?: throwError("PEM_read_bio_RSAPrivateKey fail")
            }
          val e = BigNum(RSA_get0_e(rsa) ?: throwError("RSA_get0_e fail"))
          val n = BigNum(RSA_get0_n(rsa) ?: throwError("RSA_get0_n fail"))
          val d = BigNum(RSA_get0_d(rsa) ?: throwError("RSA_get0_d fail"))
          val p = BigNum(RSA_get0_p(rsa) ?: throwError("RSA_get0_p fail"))
          val q = BigNum(RSA_get0_q(rsa) ?: throwError("RSA_get0_q fail"))
          println("e=$e")
          println("n=$n")
          println("d=$d")
          println("p=$p")
          println("q=$q")
          RSAPrivateKey(
            n = n.toBigInt(),
            e = e.toBigInt(),
            d = d.toBigInt(),
          )
        }

        is RSAPrivateKeySpec -> {
          RSAPrivateKey(
            n = encodedKeySpec.modulus,
            e = BigInteger.fromInt(65537),
            d = encodedKeySpec.privateExponent,
          )
        }

        else -> throw SecurityException("Creating RSAPrivateKey from ${encodedKeySpec::class.simpleName} not supported")
      }
  }

  actual override val algorithm: KeyAlgorithm
    get() = KeyAlgorithm.RSA
  actual override val data: ByteArray
    get() {
      val rsa = RSA_new() ?: throwError("RSA_new fail")
      val eNum = e.toBigNum()
      val dNum = d.toBigNum()
      val nNum = n.toBigNum()
      RSA_set0_key(
        r = rsa,
        n = nNum.ptr,
        e = eNum.ptr,
        d = dNum.ptr,
      ).checkTrue("RSA_set0_key fail") {
        nNum.free()
        eNum.free()
        dNum.free()
      }

      println("n=${BigNum(RSA_get0_n(rsa)!!)}")
      println("e=${BigNum(RSA_get0_e(rsa)!!)}")
      println("d=${BigNum(RSA_get0_d(rsa)!!)}")
      RSA_check_key(rsa).checkTrue("RSA_check_key faild")

      return Bio.mem().use { b ->
        println("Error: ${getSslError()}")
        PEM_write_bio_RSAPrivateKey(b.self, rsa, null, null, 0, null, null)
          .checkTrue("PEM_write_bio_RSAPrivateKey fail") {
            RSA_free(rsa)
            nNum.free()
            eNum.free()
            dNum.free()
          }
        RSA_free(rsa)
        val data = b.toByteArray()
        val str =
          data.decodeToString().replace("\n", "").removePrefix("-----BEGIN RSA PRIVATE-----")
            .removeSuffix("-----END RSA PRIVATE-----")
        Base64.decode(str)
      }
    }
  actual override val format: String
    get() = "X.509"
}
