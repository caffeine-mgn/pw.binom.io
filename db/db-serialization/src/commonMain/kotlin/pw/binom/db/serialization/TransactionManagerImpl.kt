package pw.binom.db.serialization

import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection
import kotlin.coroutines.*

class TransactionContextElement(val connection: PooledAsyncConnection) :
    CoroutineContext.Element {
    var rollbackOnly = false
    var transactionStarted: Boolean = false
    override val key: CoroutineContext.Key<TransactionContextElement>
        get() = TransactionContextElementKey
}

object TransactionContextElementKey : CoroutineContext.Key<TransactionContextElement>

private suspend fun getCurrentTransactionContext():TransactionContextElement? =
    coroutineContext[TransactionContextElementKey]
//    suspendCoroutine<TransactionContextElement?> {
//        it.resume(it.context[TransactionContextElementKey])
//    }

class TransactionManagerImpl(val connectionPool: AsyncConnectionPool) : TransactionManager {
    override suspend fun <T> re(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContext()
        return when {
            txContext == null -> {
                connectionPool.borrow {
                    val cc = TransactionContextElement(this)
                    beginTransaction()
                    cc.transactionStarted = true
                    try {
                        val result = suspendCoroutine<T> { con ->
                            function.startCoroutine(this, object : Continuation<T> {
                                override val context: CoroutineContext = con.context + cc

                                override fun resumeWith(result: Result<T>) {
                                    con.resumeWith(result)
                                }
                            })
                        }
                        if (cc.rollbackOnly) {
                            rollback()
                        } else {
                            commit()
                        }
                        result
                    } catch (e: Throwable) {
                        if (!cc.rollbackOnly) {
                            try {
                                rollback()
                            } catch (ex: Throwable) {
                                ex.addSuppressed(e)
                                throw ex
                            }
                        }
                        throw e
                    } finally {
                        cc.rollbackOnly = false
                        cc.transactionStarted = false
                    }
                }
            }
            !txContext.transactionStarted -> {
                txContext.connection.beginTransaction()
                txContext.transactionStarted = true
                try {
                    val result = function(txContext.connection)
                    if (txContext.rollbackOnly) {
                        txContext.connection.rollback()
                    } else {
                        txContext.connection.commit()
                    }
                    result
                } catch (e: Throwable) {
                    if (!txContext.rollbackOnly) {
                        try {
                            txContext.connection.rollback()
                        } catch (ex: Throwable) {
                            ex.addSuppressed(e)
                            throw ex
                        }
                    }
                    throw  e
                } finally {
                    txContext.transactionStarted = false
                    txContext.rollbackOnly = false
                }
            }
            else -> try {
                function(txContext.connection)
            } catch (e: Throwable) {
                txContext.rollbackOnly = true
                throw e
            }
        }
    }


    override suspend fun <T> new(function: suspend (PooledAsyncConnection) -> T): T {
        return connectionPool.borrow {
            val cc = TransactionContextElement(this)
            beginTransaction()
            cc.transactionStarted = true
            try {
                val result = suspendCoroutine<T> { con ->
                    val newContext = con.context.minusKey(TransactionContextElementKey) + cc
                    function.startCoroutine(this, object : Continuation<T> {
                        override val context: CoroutineContext = newContext
                        override fun resumeWith(result: Result<T>) {
                            con.resumeWith(result)
                        }
                    })
                }
                if (cc.rollbackOnly) {
                    rollback()
                } else {
                    commit()
                }
                result
            } catch (e: Throwable) {
                try {
                    rollback()
                } catch (ex: Throwable) {
                    ex.addSuppressed(e)
                    throw ex
                }
                throw e
            } finally {
                cc.rollbackOnly = false
                cc.transactionStarted = false
            }
        }
    }

    override suspend fun <T> su(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContext()

        return when (txContext) {
            null -> {
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
            }
            else -> try {
                function(txContext.connection)
            } catch (e: Throwable) {
                if (txContext.transactionStarted) {
                    txContext.rollbackOnly = true
                }
                throw e
            }
        }
    }
}