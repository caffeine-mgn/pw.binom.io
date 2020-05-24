package pw.binom.db.sqlite

import cnames.structs.sqlite3_stmt
import kotlinx.cinterop.*
import platform.internal_sqlite.*
import platform.posix.free
import pw.binom.db.Connection
import pw.binom.db.PreparedStatement
import pw.binom.db.Statement
import pw.binom.io.IOException
import pw.binom.io.file.File

actual class SQLiteConnector private constructor(val ctx: CPointer<CPointerVar<sqlite3>>) : Connection {
    actual companion object {
        actual fun openFile(file: File): SQLiteConnector =
                open(file.path)

        private fun open(path: String): SQLiteConnector {
//            val ctx = platform.posix.malloc(sizeOf<SqliteDataBase>().convert())!!.reinterpret<SqliteDataBase>()
            val ctx = platform.posix.malloc(sizeOf<CPointerVar<sqlite3>>().convert())!!.reinterpret<CPointerVar<sqlite3>>()

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
    }

    override fun createStatement(): Statement =
            SQLiteStatement(this)

    override fun prepareStatement(query: String): PreparedStatement {
        val stmt = platform.posix.malloc(sizeOf<CPointerVar<sqlite3_stmt>>().convert())!!
                .reinterpret<CPointerVar<sqlite3_stmt>>()
        sqlite3_prepare_v3(ctx.pointed.value, query, -1, 0u, stmt, null)
        return SQLitePrepareStatement(this, stmt)
    }

    override fun close() {
        sqlite3_close(ctx.pointed.value)
        free(ctx)
    }
}

internal val callback = staticCFunction<COpaquePointer?, Int, CPointer<CPointerVar<ByteVar>>?, CPointer<CPointerVar<ByteVar>>?, Int> { notUsed: COpaquePointer?, argc: Int, argv: CPointer<CPointerVar<ByteVar>>?, azColName: CPointer<CPointerVar<ByteVar>>? ->
    val resultSet = notUsed!!.asStableRef<SQLiteResultSetV1>().get()

    if (!resultSet.columnsInserted) {
        val out = ArrayList<String>(argc)
        (0 until argc).forEach {
            out += azColName!![it]!!.toKStringFromUtf8()
        }
        resultSet.columns1 = out
        resultSet.columnsInserted = true
    }

    resultSet.addRecord(
            Array(argc) {
                argv!!.get(it)?.toKStringFromUtf8()
            }
    )
    0
}