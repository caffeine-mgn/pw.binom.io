package pw.binom.db.async.pool

import pw.binom.db.async.AsyncPreparedStatement

class PooledAsyncPreparedStatement(
    val pooledConnection: PooledAsyncConnectionImpl,
    val preparedStatement: AsyncPreparedStatement
) :
    AsyncPreparedStatement by preparedStatement {

    override suspend fun asyncClose() {
        try {
            preparedStatement.asyncClose()
        } finally {
            pooledConnection.prepareStatements.remove(this)
        }
    }
}