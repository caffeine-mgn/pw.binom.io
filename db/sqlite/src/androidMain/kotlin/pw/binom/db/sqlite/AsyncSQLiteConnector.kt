package pw.binom.db.sqlite

import android.content.Context

suspend fun AsyncSQLiteConnector.openInternal(context: Context, name: String, mode: Int) =
    AsyncConnectionAdapter.create {
        SQLiteConnector.openInternal(context = context, name = name, mode = mode)
    }
