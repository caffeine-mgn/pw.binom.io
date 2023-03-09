package pw.binom

fun interface AsyncConsumer<T> {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun accept(value: T)
}
