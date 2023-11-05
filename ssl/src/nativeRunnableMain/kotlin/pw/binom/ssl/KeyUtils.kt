@file:OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)

package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.base64.Base64
import pw.binom.getSslError
import pw.binom.io.ByteArrayOutput
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.throwError

private val loaded = AtomicBoolean(false)

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal fun loadOpenSSL() {
  if (loaded.compareAndSet(false, true)) {
    OPENSSL_init_crypto(
      (OPENSSL_INIT_ADD_ALL_CIPHERS or OPENSSL_INIT_ADD_ALL_DIGESTS or OPENSSL_INIT_LOAD_CONFIG).convert(),
      null,
    )
  }
}

@OptIn(ExperimentalForeignApi::class)
fun createRsaFromPublicKey(data: ByteArray): CPointer<RSA> {
  val pem = "-----BEGIN PUBLIC KEY-----\n${Base64.encode(data)}\n-----END PUBLIC KEY-----\n"
  return Bio.mem(pem.encodeToByteArray()).use { priv ->
    PEM_read_bio_RSA_PUBKEY(priv.self, null, null, null)
      ?: throw IOException("Can't load public key: ${getSslError()}")
  }
}

fun createRsaFromPrivateKey(data: ByteArray): CPointer<RSA> {
  val pem = "-----BEGIN RSA PRIVATE KEY-----\n${Base64.encode(data)}\n-----END RSA PRIVATE KEY-----\n"
  return Bio.mem(pem.encodeToByteArray()).use { priv ->
    PEM_read_bio_RSAPrivateKey(priv.self, null, null, null) ?: throwError("PEM_read_bio_RSAPrivateKey fail")
  }
}

@OptIn(ExperimentalForeignApi::class)
fun createEcdsaFromPublicKey(data: ByteArray): CPointer<EC_KEY> {
//    val eckey = EC_KEY_new() ?: TODO("Can't create EC_KEY")
//    val point = BigInteger.fromByteArray(data, Sign.POSITIVE).toBigNum().use { num ->
//        val group = EC_GROUP_new_by_curve_name(NID_secp256k1) ?: TODO("Can't create group")
//        EC_POINT_bn2point(group, num.ptr, null, null)
//    } ?: TODO("Can't create point")
//    if (EC_KEY_set_public_key(eckey, point) <= 0) {
//        TODO("Can't set public key: ${getSslError()}. eckey=$eckey, point=$point")
//    }
//    return eckey
  val pem = "-----BEGIN PUBLIC KEY-----\n${Base64.encode(data)}\n-----END PUBLIC KEY-----\n"
  return Bio.mem(pem.encodeToByteArray()).use { priv ->
    PEM_read_bio_EC_PUBKEY(priv.self, null, null, null)
      ?: throw IOException("Can't load public key: ${getSslError()}")
  }
}

@OptIn(ExperimentalForeignApi::class)
fun createEcdsaFromPrivateKey(data: ByteArray): CPointer<EC_KEY> {
  val pem = "-----BEGIN EC PRIVATE KEY-----\n${Base64.encode(data)}\n-----END EC PRIVATE KEY-----\n"
  return Bio.mem(pem.encodeToByteArray()).use { priv ->
    PEM_read_bio_ECPrivateKey(priv.self, null, null, null)
      ?: throw IOException("Can't load private key: ${getSslError()}")
  }
}

fun Key.Private.load() = when (algorithm) {
  KeyAlgorithm.RSA -> createRsaFromPrivateKey(data)
  KeyAlgorithm.ECDSA -> createEcdsaFromPrivateKey(data)
}

fun Key.Public.load() = when (algorithm) {
  KeyAlgorithm.RSA -> createRsaFromPublicKey(data)
  KeyAlgorithm.ECDSA -> createEcdsaFromPublicKey(data)
}

var CPointer<RSA>.publicKey: ByteArray
  get() = Bio.mem().use { b ->
    PEM_write_bio_RSAPublicKey(b.self, this)
    b.toByteArray()
  }
  set(value) {
    Bio.mem(value).use { priv ->
      PEM_read_bio_RSAPublicKey(priv.self, reinterpret(), null, null)
        ?: throw IOException("Can't load public key")
    }
  }

var CPointer<RSA>.privateKey: ByteArray
  get() = Bio.mem().use { b ->
    PEM_write_bio_RSAPrivateKey(b.self, this, null, null, 0, null, null)
    b.toByteArray()
  }
  set(value) {
    Bio.mem(value).use { priv ->
      PEM_read_bio_RSAPrivateKey(priv.self, reinterpret(), null, null)
        ?: throw IOException("Can't load private key")
    }
  }

val CPointer<RSA>.dataSize
  get() = RSA_size(this)

var CPointer<EVP_PKEY>.rsa: CPointer<RSA>?
  get() = EVP_PKEY_get0_RSA(this)
  set(value) {
    EVP_PKEY_set1_RSA(this, value)
  }

object KeyUtils {

  fun CPointer<RSA>.loadPublic(data: ByteArray): CPointer<RSA> {
    Bio.mem(data).use { priv ->
      d2i_RSAPublicKey_bio(priv.self, reinterpret())
    }
    return this
  }

  fun CPointer<RSA>.loadPrivate(data: ByteArray): CPointer<RSA> {
    Bio.mem(data).use { priv ->
      d2i_RSAPrivateKey_bio(priv.self, reinterpret())
    }
    return this
  }

  fun createKeyPair(publicKey: ByteArray, privateKey: ByteArray): KeyGenerator.KeyPair {
    val rsa = RSA_new()!!
    Bio.mem(privateKey).use { priv ->
      d2i_RSAPrivateKey_bio(priv.self, rsa.reinterpret())
    }
    Bio.mem(publicKey).use { priv ->
      d2i_RSAPublicKey_bio(priv.self, rsa.reinterpret())
    }
    val pair = EVP_PKEY_new()!!
    EVP_PKEY_set1_RSA(pair, rsa)
    return KeyGenerator.KeyPair(pair)
  }

  fun getPublicKey(native: CPointer<EVP_PKEY>, algorithm: KeyAlgorithm): ByteArray {
    when (algorithm) {
      KeyAlgorithm.RSA -> {
        val rsa = EVP_PKEY_get1_RSA(native) ?: TODO("EVP_PKEY_get1_RSA returns null")
        val b = Bio.mem()
        i2d_RSAPublicKey_bio(b.self, rsa)
        val o = ByteArrayOutput()
        b.copyTo(o)
        b.close()
        o.data.flip()
        val array = o.data.toByteArray()
        o.close()
        return array
      }

      else -> TODO()
    }
  }

  fun getPrivateKey(native: CPointer<EVP_PKEY>, algorithm: KeyAlgorithm): ByteArray {
    when (algorithm) {
      KeyAlgorithm.RSA -> {
        val rsa = EVP_PKEY_get1_RSA(native) ?: TODO("EVP_PKEY_get1_RSA returns null")
        val b = Bio.mem()
        i2d_RSAPrivateKey_bio(b.self, rsa)
        val o = ByteArrayOutput()
        b.copyTo(o)
        b.close()
        o.data.flip()
        val array = o.data.toByteArray()
        o.close()
        return array
      }

      else -> TODO()
    }
  }
}
