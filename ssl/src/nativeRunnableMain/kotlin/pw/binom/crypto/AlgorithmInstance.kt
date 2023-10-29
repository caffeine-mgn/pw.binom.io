package pw.binom.crypto

import kotlinx.cinterop.CPointer
import platform.openssl.*

value class AlgorithmInstance(val ptr: CPointer<EVP_MD>) {
  companion object {
    fun sm3() = AlgorithmInstance(EVP_sm3()!!)
    fun sha512() = AlgorithmInstance(EVP_sha512()!!)
    fun sha256() = AlgorithmInstance(EVP_sha256()!!)
    fun sha224() = AlgorithmInstance(EVP_sha224()!!)
    fun sha384() = AlgorithmInstance(EVP_sha384()!!)
    fun sha1() = AlgorithmInstance(EVP_sha1()!!)
    fun sha3_256() = AlgorithmInstance(EVP_sha3_256()!!)
    fun sha3_224() = AlgorithmInstance(EVP_sha3_224()!!)
    fun sha3_384() = AlgorithmInstance(EVP_sha3_384()!!)
    fun sha3_512() = AlgorithmInstance(EVP_sha3_512()!!)
    fun md5() = AlgorithmInstance(EVP_md5()!!)
    fun md5Sha1() = AlgorithmInstance(EVP_md5_sha1()!!)
  }

  fun free() {
    EVP_MD_free(ptr)
  }
}
