package pw.binom.db.pool

import pw.binom.db.AsyncPreparedStatement

class PooledAsyncPreparedStatement(
    val pooledConnection: PooledAsyncConnection,
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