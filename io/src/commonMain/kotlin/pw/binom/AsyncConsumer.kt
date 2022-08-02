package pw.binom

fun interface AsyncConsumer<T> {
    suspend fun accept(value: T)
}
