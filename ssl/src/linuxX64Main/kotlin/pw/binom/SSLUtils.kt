package pw.binom

import kotlinx.cinterop.toKString
import platform.openssl.ERR_get_error
import platform.openssl.ERR_reason_error_string

fun getSslError(): String {
    val errorCode = ERR_get_error()
    val errorMsg = ERR_reason_error_string(errorCode)?.toKString()
    return errorMsg ?: "#$errorCode"
}
