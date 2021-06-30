package pw.binom.flux

class UnauthorizedException(val realm: String? = null, val service: String? = null) : RuntimeException()