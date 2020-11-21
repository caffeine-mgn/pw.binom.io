package pw.binom.db.sqlite

import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import platform.posix.free
import pw.binom.db.*
import pw.binom.db.SyncConnection
import pw.binom.io.file.File
import pw.binom.io.IOException

actual class SQLiteConnector private constructor(val ctx: CPointer<CPointerVar<sqlite3>>) : SyncConnection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector =
            open(file.path)

        private fun open(path: String): SQLiteConnector {
//            val ctx = platform.posix.malloc(sizeOf<SqliteDataBase>().convert())!!.reinterpret<SqliteDataBase>()
            val ctx =
                platform.posix.malloc(sizeOf<CPointerVar<sqlite3>>().convert())!!.reinterpret<CPointerVar<sqlite3>>()

            val vv = sqlite3_open(path, ctx)
            if (vv > 0) {
                sqlite3_errmsg(ctx.pointed.value)?.toKString()
                sqlite3_close(ctx.pointed.value)
                free(ctx)
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

    private val beginSt = prepareStatement("BEGIN")
    private val commitSt = prepareStatement("COMMIT")
    private val rollbackSt = prepareStatement("ROLLBACK")

    init {
        beginSt.executeUpdate()
    }

    override fun createStatement() =
        SQLiteStatement(this)

    override fun prepareStatement(query: String): SyncPreparedStatement {
        val stmt = platform.posix.malloc(sizeOf<CPointerVar<sqlite3_stmt>>().convert())!!
            .reinterpret<CPointerVar<sqlite3_stmt>>()
        sqlite3_prepare_v3(ctx.pointed.value, query, -1, 0u, stmt, null)
        return SQLitePrepareStatement(this, stmt)
    }

    override fun commit() {
        commitSt.executeUpdate()
        beginSt.executeUpdate()
    }

    override fun rollback() {
        rollbackSt.executeUpdate()
        beginSt.executeUpdate()
    }

    override val type: String
        get() = TYPE

    override fun close() {
        commitSt.executeUpdate()
        commitSt.close()
        rollbackSt.close()
        beginSt.close()
        val r = sqlite3_close(ctx.pointed.value)
        if (r != SQLITE_OK) {
            checkSqlCode(r)
        }
        free(ctx)
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