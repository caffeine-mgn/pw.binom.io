package pw.binom.io

open class BindException(message: String? = null, cause: Throwable? = null):IOException(message=message,cause = cause)