package pw.binom.db.serialization

interface TransactionManager<CONNECTION> {
    suspend fun <T> re(function: suspend (CONNECTION) -> T): T
    suspend fun <T> new(function: suspend (CONNECTION) -> T): T
    suspend fun <T> su(function: suspend (CONNECTION) -> T): T

    suspend fun onSuccess(action: suspend () -> Unit)
    suspend fun onRollback(action: suspend () -> Unit)
}
