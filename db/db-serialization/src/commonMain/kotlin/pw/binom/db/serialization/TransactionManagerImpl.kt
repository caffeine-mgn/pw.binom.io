package pw.binom.db.serialization

import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection
import kotlin.coroutines.*

class TransactionContextElement(val connection: PooledAsyncConnection) : CoroutineContext.Element {
    var rollbackOnly = false
    var transactionStarted: Boolean = false
    val successFullActions = ArrayList<suspend () -> Unit>()
    val rollbackActions = ArrayList<suspend () -> Unit>()
    suspend fun executeSuccessActions() {
        successFullActions.forEach {
            it()
        }
    }

    suspend fun executeRollbackActions() {
        rollbackActions.forEach {
            it()
        }
    }

    override val key: CoroutineContext.Key<TransactionContextElement>
        get() = TransactionContextElementKey
}

object TransactionContextElementKey : CoroutineContext.Key<TransactionContextElement>

private suspend fun getCurrentTransactionContextOrNull(): TransactionContextElement? =
    coroutineContext[TransactionContextElementKey]

private suspend fun getCurrentTransactionContext() = getCurrentTransactionContextOrNull()
    ?: throw IllegalStateException("This function should calling inside re,su or new functions")

class TransactionManagerImpl(val connectionPool: AsyncConnectionPool) : TransactionManager<PooledAsyncConnection> {
    override suspend fun <T> re(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContextOrNull()
        return when {
            txContext == null -> {
                connectionPool.borrow {
                    val cc = TransactionContextElement(this)
                    beginTransaction()
                    cc.transactionStarted = true
                    var rollbackExecuted = false
                    try {
                        val result = suspendCoroutine<T> { con ->
                            function.startCoroutine(
                                this,
                                object : Continuation<T> {
                                    override val context: CoroutineContext = con.context + cc

                                    override fun resumeWith(result: Result<T>) {
                                        con.resumeWith(result)
                                    }
                                }
                            )
                        }
                        if (cc.rollbackOnly) {
                            rollback()
                            rollbackExecuted = true
                            cc.executeRollbackActions()
                        } else {
                            cc.executeSuccessActions()
                            commit()
                        }
                        result
                    } catch (e: Throwable) {
                        e.wrap {
                            if (!rollbackExecuted) {
                                rollback()
                                cc.executeRollbackActions()
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
                var rollbackExecuted = false
                try {
                    val result = function(txContext.connection)
                    if (txContext.rollbackOnly) {
                        txContext.connection.rollback()
                        rollbackExecuted = true
                        txContext.executeRollbackActions()
                    } else {
                        txContext.executeSuccessActions()
                        txContext.connection.commit()
                    }
                    result
                } catch (e: Throwable) {
                    e.wrap {
                        if (!rollbackExecuted) {
                            txContext.connection.rollback()
                            txContext.executeRollbackActions()
                        }
                    }
                    throw e
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
            var rollbackExecuted = false
            try {
                val result = suspendCoroutine<T> { con ->
                    val newContext = con.context.minusKey(TransactionContextElementKey) + cc
                    function.startCoroutine(
                        this,
                        object : Continuation<T> {
                            override val context: CoroutineContext = newContext
                            override fun resumeWith(result: Result<T>) {
                                con.resumeWith(result)
                            }
                        }
                    )
                }
                if (cc.rollbackOnly) {
                    rollback()
                    rollbackExecuted = true
                    cc.executeRollbackActions()
                } else {
                    cc.executeSuccessActions()
                    commit()
                }
                result
            } catch (e: Throwable) {
                e.wrap {
                    if (!rollbackExecuted) {
                        rollback()
                        cc.executeRollbackActions()
                    }
                }
                throw e
            } finally {
                cc.rollbackOnly = false
                cc.transactionStarted = false
            }
        }
    }

    override suspend fun <T> su(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContextOrNull()

        return when (txContext) {
            null -> {
                connectionPool.borrow {
                    val cc = TransactionContextElement(this)
                    suspendCoroutine { con ->
                        function.startCoroutine(
                            this,
                            object : Continuation<T> {
                                override val context: CoroutineContext = con.context + cc
                                override fun resumeWith(result: Result<T>) {
                                    con.resumeWith(result)
                                }
                            }
                        )
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

    override suspend fun onSuccess(action: suspend () -> Unit) {
        val txContext = getCurrentTransactionContext()
        txContext.successFullActions += action
    }

    override suspend fun onRollback(action: suspend () -> Unit) {
        val txContext = getCurrentTransactionContext()
        txContext.rollbackActions += action
    }
}
