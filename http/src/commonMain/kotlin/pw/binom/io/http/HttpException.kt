package pw.binom.io.http

class HttpException : Exception {
    val code: Int

    constructor(code: Int) : super() {
        this.code = code
    }

    constructor(code: Int, message: String?) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.code = code
    }

    constructor(code: Int, cause: Throwable?) : super(cause) {
        this.code = code
    }
}
