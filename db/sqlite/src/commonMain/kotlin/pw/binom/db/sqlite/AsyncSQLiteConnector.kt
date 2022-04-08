package pw.binom.db.sqlite

import pw.binom.io.file.File

object AsyncSQLiteConnector {
    suspend fun openFile(file: File) =
        AsyncConnectionAdapter.create {
            SQLiteConnector.openFile(file)
        }

    suspend fun memory(name: String? = null) =
        AsyncConnectionAdapter.create {
            SQLiteConnector.memory(name)
        }

    val TYPE: String
        get() = SQLiteConnector.TYPE
}
