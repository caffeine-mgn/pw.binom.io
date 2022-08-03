package pw.binom

actual class Cleaner {
    actual companion object {
        actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
            val c = Cleaner()
            return c
        }
    }
}
