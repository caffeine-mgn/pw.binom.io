package pw.binom.network

import pw.binom.io.IOException

open class UnknownHostException(host: String) : IOException(message = host)
