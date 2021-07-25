package pw.binom.db.serialization

import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection
import kotlin.coroutines.*

class TransactionContextElement(val connection: PooledAsyncConnection) :
    CoroutineContext.Element {
    var rollback = false
    var transactionStarted: Boolean = false
    override val key: CoroutineContext.Key<TransactionContextElement>
        get() = TransactionContextElementKey
}

object TransactionContextElementKey : CoroutineContext.Key<TransactionContextElement>

private suspend fun getCurrentTransactionContext() =
    suspendCoroutine<TransactionContextElement?> {
        it.resume(it.context[TransactionContextElementKey])
    }

class TransactionManagerImpl(val connectionPool: AsyncConnectionPool) : TransactionManager {
    override suspend fun <T> re(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContext()

        return if (txContext == null) {
            connectionPool.borrow {
                val cc = TransactionContextElement(this)
                try {
                    beginTransaction()
                    cc.transactionStarted = true
                    val result = suspendCoroutine<T> { con ->
                        function.startCoroutine(this, object : Continuation<T> {
                            override val context: CoroutineContext = con.context + cc

                            override fun resumeWith(result: Result<T>) {
                                con.resumeWith(result)
                            }
                        })
                    }
                    if (cc.rollback) {
                        rollback()
                    } else {
                        commit()
                    }
                    result
                } catch (e: Throwable) {
                    rollback()
                    throw e
                } finally {
                    cc.rollback = false
                    cc.transactionStarted = false
                }
            }
        } else {
            if (!txContext.transactionStarted) {
                try {
                    txContext.connection.beginTransaction()
                    val result = function(txContext.connection)
                    if (txContext.rollback) {
                        txContext.connection.rollback()
                    } else {
                        txContext.connection.commit()
                    }
                    result
                } catch (e: Throwable) {
                    txContext.connection.rollback()
                    throw  e
                }
            } else {
                function(txContext.connection)
            }
        }
    }

    override suspend fun <T> new(function: suspend (PooledAsyncConnection) -> T): T {
        return connectionPool.borrow {
            val cc = TransactionContextElement(this)
            beginTransaction()
            cc.transactionStarted = true
            suspendCoroutine<T> { con ->
                val newContext = con.context.minusKey(TransactionContextElementKey) + cc
                function.startCoroutine(this, object : Continuation<T> {
                    override val context: CoroutineContext = newContext

                    override fun resumeWith(result: Result<T>) {
                        con.resumeWith(result)
                    }
                })
            }
            try {
                val result = function(this)
                if (cc.rollback) {
                    rollback()
                } else {
                    commit()
                }
                result
            } catch (e: Throwable) {
                rollback()
                throw e
            } finally {
                cc.rollback = false
                cc.transactionStarted = false
            }
        }
    }

    override suspend fun <T> su(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContext()

        return if (txContext == null) {
            connectionPool.borrow {
                val cc = TransactionContextElement(this)
                suspendCoroutine { con ->
                    function.startCoroutine(this, object : Continuation<T> {
                        override val context: CoroutineContext = con.context + cc

                        override fun resumeWith(result: Result<T>) {
                            con.resumeWith(result)
                        }
                    })
                }
            }
        } else {
            function(txContext.connection)
        }
    }
}