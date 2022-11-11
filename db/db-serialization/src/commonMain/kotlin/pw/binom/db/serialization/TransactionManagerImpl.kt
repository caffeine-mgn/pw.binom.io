package pw.binom.db.serialization

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool
import kotlin.coroutines.*

class TransactionContext {
    lateinit var connection: PooledAsyncConnection
    var rollbackOnly = false
    var transactionStarted: Boolean = false
    val successFullActions = defaultMutableList<suspend () -> Unit>()
    val rollbackActions = defaultMutableList<suspend () -> Unit>()
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
}

class TransactionContextElement(val ctx: TransactionContext) : CoroutineContext.Element {
    val connections = defaultMutableMap<AsyncConnectionPool, TransactionContext>()

    override val key: CoroutineContext.Key<TransactionContextElement>
        get() = TransactionContextElementKey

    companion object {
        suspend fun <T> create(
            connectionPool: AsyncConnectionPool,
            element: TransactionContextElement?,
            func: suspend (TransactionContextElement, TransactionContext) -> T
        ): T {
            val ctx = TransactionContext() // New ctx
            val newElement = TransactionContextElement(ctx) // New coroutine context
            element?.connections?.forEach { (pool, ctx) -> // copy old contexts from other pools
                if (pool !== connectionPool) {
                    newElement.connections[pool] = ctx
                }
            }

            newElement.connections[connectionPool] = ctx // set connect of current new context
            return func(newElement, ctx)
        }
    }
}

object TransactionContextElementKey : CoroutineContext.Key<TransactionContextElement>

private suspend fun getCurrentTransactionContextOrNull(): TransactionContextElement? =
    coroutineContext[TransactionContextElementKey]

private suspend fun getCurrentTransactionContext() = getCurrentTransactionContextOrNull()
    ?: throw IllegalStateException("This function should calling inside re,su or new functions")

interface ConnectionCreator {
    val isConnected: Boolean
    suspend fun getConnect(): PooledAsyncConnection
}

private class ConnectionCreatorImpl(val connectionPool: AsyncConnectionPool) : ConnectionCreator {
    private var connection: PooledAsyncConnection? = null

    class Factory(val connectionPool: AsyncConnectionPool) : ObjectFactory<ConnectionCreatorImpl> {
        override fun allocate(pool: ObjectPool<ConnectionCreatorImpl>): ConnectionCreatorImpl =
            ConnectionCreatorImpl(connectionPool)

        override fun deallocate(value: ConnectionCreatorImpl, pool: ObjectPool<ConnectionCreatorImpl>) {
            // Do nothing
        }

        override fun reset(value: ConnectionCreatorImpl, pool: ObjectPool<ConnectionCreatorImpl>) {
            super.reset(value, pool)
            value.connection = null
        }
    }

    override val isConnected: Boolean
        get() = connection != null

    override suspend fun getConnect(): PooledAsyncConnection {
        var connection = connection
        if (connection == null) {
            connection = connectionPool.getConnection()
            this.connection = connection
            return connection
        }
        return connection
    }
}

class TransactionManagerImpl(val connectionPool: AsyncConnectionPool) : TransactionManager<PooledAsyncConnection> {
    //    private val pool = GenericObjectPool(ConnectionCreatorImpl.Factory(connectionPool))
    override suspend fun <T> re(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContextOrNull()
        val context = txContext?.connections?.get(connectionPool)
        return when {
            context == null -> {
                connectionPool.borrow {
                    TransactionContextElement.create(connectionPool, txContext) { element, ctx ->
                        ctx.connection = this
                        beginTransaction()
                        ctx.transactionStarted = true
                        var rollbackExecuted = false
                        try {
                            val result = suspendCoroutine<T> { con ->
                                function.startCoroutine(
                                    this,
                                    object : Continuation<T> {
                                        override val context: CoroutineContext = con.context + element
                                        override fun resumeWith(result: Result<T>) {
                                            con.resumeWith(result)
                                        }
                                    }
                                )
                            }
                            if (ctx.rollbackOnly) {
                                rollback()
                                rollbackExecuted = true
                                ctx.executeRollbackActions()
                            } else {
                                ctx.executeSuccessActions()
                                commit()
                            }
                            result
                        } catch (e: Throwable) {
                            e.wrap {
                                if (!rollbackExecuted) {
                                    rollback()
                                    ctx.executeRollbackActions()
                                }
                            }
                            throw e
                        } finally {
                            ctx.rollbackOnly = false
                            ctx.transactionStarted = false
                        }
                    }
                }
            }

            !context.transactionStarted -> {
                context.connection.beginTransaction()
                context.transactionStarted = true
                var rollbackExecuted = false
                try {
                    val result = function(context.connection)
                    if (context.rollbackOnly) {
                        context.connection.rollback()
                        rollbackExecuted = true
                        context.executeRollbackActions()
                    } else {
                        context.executeSuccessActions()
                        context.connection.commit()
                    }
                    result
                } catch (e: Throwable) {
                    e.wrap {
                        if (!rollbackExecuted) {
                            context.connection.rollback()
                            context.executeRollbackActions()
                        }
                    }
                    throw e
                } finally {
                    context.transactionStarted = false
                    context.rollbackOnly = false
                }
            }

            else -> try {
                function(context.connection)
            } catch (e: Throwable) {
                context.rollbackOnly = true
                throw e
            }
        }
    }

    override suspend fun <T> new(function: suspend (PooledAsyncConnection) -> T): T {
        return connectionPool.borrow {
            TransactionContextElement.create(connectionPool, getCurrentTransactionContextOrNull()) { element, ctx ->
                ctx.connection = this
                beginTransaction()
                ctx.transactionStarted = true
                var rollbackExecuted = false
                try {
                    val result = suspendCoroutine<T> { con ->
                        val newContext = con.context.minusKey(TransactionContextElementKey) + element
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
                    if (ctx.rollbackOnly) {
                        rollback()
                        rollbackExecuted = true
                        ctx.executeRollbackActions()
                    } else {
                        ctx.executeSuccessActions()
                        commit()
                    }
                    result
                } catch (e: Throwable) {
                    e.wrap {
                        if (!rollbackExecuted) {
                            rollback()
                            ctx.executeRollbackActions()
                        }
                    }
                    throw e
                } finally {
                    ctx.rollbackOnly = false
                    ctx.transactionStarted = false
                }
            }
        }
    }

    override suspend fun <T> su(function: suspend (PooledAsyncConnection) -> T): T {
        val txContext = getCurrentTransactionContextOrNull()
        val context = txContext?.connections?.get(connectionPool)
        return when (context) {
            null -> {
                connectionPool.borrow {
                    TransactionContextElement.create(
                        connectionPool,
                        getCurrentTransactionContextOrNull()
                    ) { element, ctx ->
                        ctx.connection = this
                        suspendCoroutine { con ->
                            function.startCoroutine(
                                this,
                                object : Continuation<T> {
                                    override val context: CoroutineContext = con.context + element
                                    override fun resumeWith(result: Result<T>) {
                                        con.resumeWith(result)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            else -> try {
                function(context.connection)
            } catch (e: Throwable) {
                if (context.transactionStarted) {
                    context.rollbackOnly = true
                }
                throw e
            }
        }
    }

    override suspend fun <T> no(function: suspend (PooledAsyncConnection) -> T): T {
        return connectionPool.borrow {
            TransactionContextElement.create(connectionPool, getCurrentTransactionContextOrNull()) { element, ctx ->
                ctx.connection = this
                ctx.transactionStarted = false
                try {
                    val result = suspendCoroutine<T> { con ->
                        val newContext = con.context.minusKey(TransactionContextElementKey) + element
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
                    ctx.executeSuccessActions()
                    result
                } catch (e: Throwable) {
                    e.wrap {
                        ctx.executeRollbackActions()
                    }
                    throw e
                } finally {
                    ctx.rollbackOnly = false
                    ctx.transactionStarted = false
                }
            }
        }
    }

    override suspend fun onSuccess(action: suspend () -> Unit) {
        val txContext = getCurrentTransactionContext()
        txContext.ctx.successFullActions += action
    }

    override suspend fun onRollback(action: suspend () -> Unit) {
        val txContext = getCurrentTransactionContext()
        txContext.ctx.rollbackActions += action
    }
}
