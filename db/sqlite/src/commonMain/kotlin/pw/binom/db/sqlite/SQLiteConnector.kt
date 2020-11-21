package pw.binom.db.sqlite

import pw.binom.db.Connection
import pw.binom.db.SyncConnection
import pw.binom.io.file.File

expect class SQLiteConnector : SyncConnection {
    companion object {
        fun openFile(file: File): SQLiteConnector
        fun memory(name: String? = null): SQLiteConnector
        val TYPE:String
    }
}