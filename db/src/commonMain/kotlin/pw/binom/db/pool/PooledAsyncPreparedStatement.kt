package pw.binom.db.pool

import pw.binom.db.AsyncPreparedStatement

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