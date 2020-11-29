package pw.binom.network

import pw.binom.io.IOException

open class SocketConnectException(message: String? = null, cause: Throwable? = null) : IOException(message = message, cause = cause)