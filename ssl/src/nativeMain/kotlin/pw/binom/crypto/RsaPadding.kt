package pw.binom.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.RSA_NO_PADDING
import platform.openssl.RSA_PKCS1_PADDING
import platform.openssl.RSA_PKCS1_PADDING_SIZE

@OptIn(ExperimentalForeignApi::class)
actual enum class RsaPadding(val id: Int, val size: Int) {
  PKCS1Padding(id = RSA_PKCS1_PADDING, size = RSA_PKCS1_PADDING_SIZE),
  NoPadding(id = RSA_NO_PADDING, size = 0),
}
