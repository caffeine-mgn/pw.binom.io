@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)

package pw.binom

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.toKString
import platform.openssl.ERR_get_error
import platform.openssl.ERR_reason_error_string
import pw.binom.security.SecurityException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun getSslError(): String {
  val errorCode = ERR_get_error()
  val errorMsg = ERR_reason_error_string(errorCode)?.toKString()
  return if (errorMsg != null) "#$errorCode $errorMsg" else "#$errorCode"
}

fun throwError(msg: String): Nothing = throw SecurityException("$msg: ${getSslError()}")

@OptIn(ExperimentalContracts::class)
inline fun throwError(msg: String, onFail: () -> Unit): Nothing {
  contract {
    callsInPlace(onFail,InvocationKind.AT_MOST_ONCE)
  }
  try {
    onFail()
    throw SecurityException("$msg: ${getSslError()}")
  } catch (e: Throwable) {
    val ex = SecurityException("$msg: ${getSslError()}")
    ex.addSuppressed(e)
    throw ex
  }
}

fun Int.checkTrue(msg: String): Int {
  if (this <= 0) {
    throw SecurityException("$msg: ${getSslError()}")
  }
  return this
}

@OptIn(ExperimentalContracts::class)
inline fun Int.checkTrue(msg: String, onFail: () -> Unit): Int {
  contract {
    callsInPlace(onFail,InvocationKind.AT_MOST_ONCE)
  }
  if (this <= 0) {
    try {
      onFail()
      throw SecurityException("$msg: ${getSslError()}")
    } catch (e: Throwable) {
      val ex = SecurityException("$msg: ${getSslError()}")
      ex.addSuppressed(e)
      throw ex
    }
  }
  return this
}
