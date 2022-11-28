package pw.binom.cleaner

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class Cleaner {
    companion object {
        fun <T> create(value: T, func: (T) -> Unit): Cleaner
    }
}
