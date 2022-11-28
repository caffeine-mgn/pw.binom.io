package pw.binom.url

class InvalidPathException(val path: String) : RuntimeException() {
    override val message: String
        get() = "Invalid path \"$path\""
}
