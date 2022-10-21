package pw.binom.io.db.kmigrator

import pw.binom.collections.defaultMutableList
import pw.binom.db.async.pool.PooledAsyncConnection

class KMigratorBuilder internal constructor() {
    internal val steps = defaultMutableList<KMigrator.Step>()

    fun step(id: String, sql: String) {
        steps += KMigrator.Step.StepSQL(id = id, sql = sql)
    }

    fun step(id: String, func: suspend (PooledAsyncConnection) -> Unit) {
        steps += KMigrator.Step.StepFunction(id = id, func = func)
    }
}
