package pw.binom.db.sqlite

import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.db.SQLException
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.doFreeze
import pw.binom.io.ClosedException
import pw.binom.io.IOException
import pw.binom.io.file.File

actual class SQLiteConnector private constructor(val ctx: CPointer<CPointerVar<sqlite3>>) : SyncConnection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector {
//            val parent = file.parent ?: throw FileNotFoundException("File ${file.path} not found")
//            if (!parent.isExist) {
//                throw FileNotFoundException("Direction ${file.path} is not found")
//            }
//            if (!parent.isDirectory) {
//                throw FileNotFoundException("Path ${file.path} is not direction")
//            }
            return open(file.path, memory = false)
        }

        private fun open(path: String, memory: Boolean): SQLiteConnector {
            val ctx = nativeHeap.allocArray<CPointerVar<sqlite3>>(1)
            var flags = SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE
            if (memory) {
                flags = flags or SQLITE_OPEN_MEMORY
            }
            val errorNum = sqlite3_open_v2(path, ctx, flags, null)
            if (errorNum > 0) {
                val msg = sqlite3_errmsg(ctx.pointed.value)?.toKString()
                sqlite3_close(ctx.pointed.value)
                nativeHeap.free(ctx)
                throw IOException("Can't open Data Base \"$path\": $msg")
            }
            return SQLiteConnector(ctx)
        }

        actual fun memory(name: String?): SQLiteConnector {
            val path = if (name == null || name.isBlank()) "file::memory:" else "file:$name?mode=memory"
            return open(path, memory = true)
        }

        actual val TYPE: String
            get() = "SQLite"
    }

    private val closed = atomic(false)
    private val prepareStatements = HashSet<SQLitePrepareStatement>()
    private val prepareStatementsLock = SpinLock()
    private val beginSt = prepareStatement("BEGIN")
    private val commitSt = prepareStatement("COMMIT")
    private val rollbackSt = prepareStatement("ROLLBACK")
    internal fun delete(statement: SQLitePrepareStatement) {
        prepareStatementsLock.synchronize {
            prepareStatements -= statement
        }
    }

    init {
//        beginSt.executeUpdate()
        doFreeze()
    }

    private fun checkClosed() {
        if (closed.value) {
            throw ClosedException()
        }
    }

    override fun createStatement(): SQLiteStatement {
        checkClosed()
        return SQLiteStatement(this)
    }

    override fun prepareStatement(query: String): SyncPreparedStatement {
        println("SQLITE_VERSION=$SQLITE_VERSION")
        checkClosed()
        val stmt = nativeHeap.allocArray<CPointerVar<sqlite3_stmt>>(1)
        checkSqlCode(
            sqlite3_prepare_v3(
                ctx.pointed.value,
                query,
                -1,
                0u,
                stmt,
                null
            )
        ) { "Can't compile query \"$query\"" }
        val statement = SQLitePrepareStatement(this, stmt)
        prepareStatementsLock.synchronize {
            prepareStatements += statement
        }
        return statement
    }

    override fun beginTransaction() {
        checkClosed()
        beginSt.executeUpdate()
    }

    override fun commit() {
        checkClosed()
        commitSt.executeUpdate()
//        beginSt.executeUpdate()
    }

    override fun rollback() {
        checkClosed()
        rollbackSt.executeUpdate()
//        beginSt.executeUpdate()
    }

    override val type: String
        get() = TYPE

    override val isConnected: Boolean
        get() = !closed.value

    override val dbInfo: DatabaseInfo
        get() = SQLiteSQLDatabaseInfo

    override fun close() {
        println("Closing SQLite Connection")
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        val l = ArrayList(prepareStatements)
        prepareStatements.clear()
        l.forEach {
            it.close()
        }
        try {
            if (sqlite3_get_autocommit(ctx.pointed.value) == 0) {
                rollbackSt.executeUpdate()
            }
            commitSt.close()
            rollbackSt.close()
            beginSt.close()
            val r = sqlite3_close(ctx.pointed.value)
            if (r != SQLITE_OK) {
                checkSqlCode(r)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            nativeHeap.free(ctx)
        }
    }
}

internal fun SQLiteConnector.checkSqlCode(code: Int, func: (() -> String)? = null) {
    fun detail() = if (func == null) "" else ": ${func()}"
    fun msg() = sqlite3_errmsg(this.ctx.pointed!!.value)?.toKStringFromUtf8() ?: "Unknown Error"
    when (code) {
        SQLITE_OK, SQLITE_DONE, SQLITE_ROW -> return
        SQLITE_BUSY -> throw SQLException("Database is Busy" + detail())
        SQLITE_ERROR -> throw SQLException(msg() + detail())
        SQLITE_MISUSE -> throw SQLException("Database is Misuse" + detail())
        SQLITE_CONSTRAINT -> throw SQLException("Constraint: ${msg()}${detail()}")
        else -> throw SQLException("SQL Code: $code${detail()}")
    }
}
