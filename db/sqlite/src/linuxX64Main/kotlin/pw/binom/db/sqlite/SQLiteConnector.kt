package pw.binom.db.sqlite

import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.db.*
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.sync.SyncConnection
import pw.binom.db.sync.SyncPreparedStatement
import pw.binom.doFreeze
import pw.binom.io.ClosedException
import pw.binom.io.file.File
import pw.binom.io.IOException

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
            return open(file.path)
        }

        private fun open(path: String): SQLiteConnector {
            val ctx = nativeHeap.allocArray<CPointerVar<sqlite3>>(1)
            val errorNum = sqlite3_open_v2(path, ctx, SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE, null)
            if (errorNum > 0) {
                sqlite3_errmsg(ctx.pointed.value)?.toKString()
                sqlite3_close(ctx.pointed.value)
                nativeHeap.free(ctx)
                throw IOException("Can't open Data Base $path")
            }
            return SQLiteConnector(ctx)
        }

        actual fun memory(name: String?): SQLiteConnector {
            val path = if (name == null || name.isBlank()) "file::memory:" else "file:$name?mode=memory"
            return open(path)
        }

        actual val TYPE: String
            get() = "SQLite"
    }

    private val closed = AtomicBoolean(false)
    private val beginSt = prepareStatement("BEGIN")
    private val commitSt = prepareStatement("COMMIT")
    private val rollbackSt = prepareStatement("ROLLBACK")

    init {
        beginSt.executeUpdate()
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
        checkClosed()
        val stmt = nativeHeap.allocArray<CPointerVar<sqlite3_stmt>>(1)
        sqlite3_prepare_v3(ctx.pointed.value, query, -1, 0u, stmt, null)
        return SQLitePrepareStatement(this, stmt)
    }

    override fun commit() {
        checkClosed()
        commitSt.executeUpdate()
        beginSt.executeUpdate()
    }

    override fun rollback() {
        checkClosed()
        rollbackSt.executeUpdate()
        beginSt.executeUpdate()
    }

    override val type: String
        get() = TYPE

    override val isConnected: Boolean
        get() = !closed.value

    override val dbInfo: DatabaseInfo
        get() = SQLiteSQLDatabaseInfo

    override fun close() {
        checkClosed()
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
            closed.value = true
            nativeHeap.free(ctx)
        }
    }
}

internal fun SQLiteConnector.checkSqlCode(code: Int) {
    fun msg() = sqlite3_errmsg(this.ctx.pointed!!.value)?.toKStringFromUtf8() ?: "Unknown Error"
    when (code) {
        SQLITE_OK, SQLITE_DONE, SQLITE_ROW -> return
        SQLITE_BUSY -> throw SQLException("Database is Busy")
        SQLITE_ERROR -> throw SQLException(msg())
        SQLITE_MISUSE -> throw SQLException("Database is Misuse")
        SQLITE_CONSTRAINT -> throw SQLException("Constraint: ${msg()}")
        else -> throw SQLException("SQL Code: $code")
    }
}