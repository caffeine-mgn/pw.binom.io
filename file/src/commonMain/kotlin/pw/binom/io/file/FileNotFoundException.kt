package pw.binom.io.file

import pw.binom.io.IOException

open class FileNotFoundException(message: String? = null, cause: Throwable? = null) : IOException(message, cause)
