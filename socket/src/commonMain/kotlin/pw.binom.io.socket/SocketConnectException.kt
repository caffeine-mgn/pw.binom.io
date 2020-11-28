package pw.binom.io.socket

import pw.binom.io.IOException

class SocketConnectException(message: String? = null, cause: Throwable? = null) : IOException(message = message, cause = cause)