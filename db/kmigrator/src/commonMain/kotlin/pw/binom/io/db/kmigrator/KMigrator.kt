package pw.binom.io.db.kmigrator

import pw.binom.collections.defaultMutableSet
import pw.binom.date.DateTime
import pw.binom.db.async.forEach
import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.db.async.pool.SelectQuery
import pw.binom.db.async.pool.execute
import pw.binom.io.use
import pw.binom.logger.Logger
import pw.binom.logger.debug

fun kmigrator(
    table: String = "kmigrator",
    schema: String? = null,
    maxIdLength: Int = 100,
    func: KMigratorBuilder.() -> Unit
): KMigrator {
    val builder = KMigratorBuilder()
    func(builder)
    return KMigrator(
        table = table,
        schema = schema,
        steps = builder.steps,
        maxIdLength = maxIdLength,
    )
}

class KMigrator(
    table: String = "kmigrator",
    val schema: String? = null,
    val maxIdLength: Int = 100,
    val steps: List<Step>
) {
    private val logger = Logger.getLogger("KMigrator")
    private val table = "${schema.let { if (it != null) "$it." else "" }}$table"
    private val getExecutedMigrations = SelectQuery("select id from $table")

    sealed class Step {
        abstract val id: String
        abstract suspend fun execute(connection: PooledAsyncConnection)
        class StepSQL(override val id: String, val sql: String) : Step() {
            override suspend fun execute(connection: PooledAsyncConnection) {
                connection.executeUpdate(sql)
            }
        }

        class StepFunction(override val id: String, val func: suspend (PooledAsyncConnection) -> Unit) : Step() {
            override suspend fun execute(connection: PooledAsyncConnection) {
                func(connection)
            }
        }
    }

    suspend fun execute(st: AsyncConnectionPool) {
        val executedMigrations = defaultMutableSet<String>()
        st.borrow {
            executeUpdate(
                """
create table if not exists $table (
    id                 varchar($maxIdLength) not null primary key,
    create_date        timestamp             not null
);
            """
            )
            prepareStatement("insert into $table (id, create_date) values (?, ?)").use { insertRecord ->
                execute(getExecutedMigrations) {
                    it.forEach {
                        executedMigrations += it.getString(0)!!
                    }
                }

                steps.forEach {
                    if (it.id in executedMigrations) {
                        return@forEach
                    }
                    try {
                        executeUpdate("begin")
                        it.execute(this)
                        insertRecord.executeUpdate(it.id, DateTime())
                        executeUpdate("commit")
                        logger.debug("Step \"${it.id}\" successful applied")
                    } catch (e: Throwable) {
                        logger.debug("Failed apply step \"${it.id}\"", e)
                        executeUpdate("rollback")
                        throw KMigrationException(it.id, e)
                    }
                }
            }
        }
    }

    operator fun plus(kmigrator: KMigrator): KMigrator {
        if (table != kmigrator.table) {
            throw IllegalArgumentException("Other migration has different info table name")
        }
        if (schema != kmigrator.schema) {
            throw IllegalArgumentException("Other migration has different info table schema")
        }
        return KMigrator(table = table, schema = schema, maxIdLength = maxIdLength, steps = steps + kmigrator.steps)
    }
}
