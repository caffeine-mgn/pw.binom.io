package pw.binom

actual class Cleaner {
    actual companion object {
        private val native = java.lang.ref.Cleaner.create()
        actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
            val c = Cleaner()
            native.register(c) { func(value) }
            return c
        }
    }
}
