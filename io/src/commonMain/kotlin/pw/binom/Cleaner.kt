package pw.binom

expect class Cleaner {
    companion object {
        fun <T> create(value: T, func: (T) -> Unit): Cleaner
    }
}
