package pw.binom.db.postgresql.async

import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.CloseMessage
import pw.binom.db.postgresql.async.messages.frontend.SyncMessage
import pw.binom.io.AsyncCloseable
import pw.binom.io.IOException

sealed class QueryResponse {
    class Status(val status: String, val rowsAffected: Long) : QueryResponse() {
        override fun toString(): String = "Status(status: [$status])"
    }

    class Data(val connection: PGConnection) : QueryResponse(), AsyncCloseable {
        private var _meta: RowDescriptionMessage? = null
        val meta: Array<ColumnMeta>
            get() {
                checkClosed()
                return _meta!!.columns
            }
        private var closed = true
        private var ended = true
        var portalName: String? = null
        var binaryResult = false
        val isClosed
            get() = closed

        private var current: DataRowMessage? = null
        fun reset(meta: RowDescriptionMessage) {
            this._meta = meta
            closed = false
            ended = false
        }

        suspend fun next(): Boolean {
            checkClosed()
            if (ended) {
                return false
            }
            check(_meta != null)
            val msg = connection.readDesponse()
            return when (msg) {
                is DataRowMessage -> {
                    current = msg
                    true
                }

                is CommandCompleteMessage -> {
                    current = null
                    ended = true
                    connection.busy = false
                    checkType<ReadyForQueryMessage>(connection.readDesponse())
                    closeCursor()
                    false
                }

                else -> throw IOException("Unknown response type: \"$msg\" (${msg::class})")
            }
        }

        operator fun get(index: Int): ByteArray? {
            checkClosed()
            val current = current
            check(current != null)
            return current.data[index]
        }

        private suspend fun closeCursor() {
            if (portalName != null) {
                connection.sendOnly(
                    CloseMessage().also {
                        it.statement = portalName!!
                        it.portal = true
                    }
                )
                connection.sendOnly(SyncMessage)
                checkType<CloseCompleteMessage>(connection.readDesponse())
                checkType<ReadyForQueryMessage>(connection.readDesponse())
//                check(connection.readDesponse() is CloseCompleteMessage)
//                check(connection.readDesponse() is ReadyForQueryMessage)
            }
        }

        override suspend fun asyncClose() {
            checkClosed()
            try {
                if (!ended) {
                    while (true) {
                        if (!next()) {
                            break
                        }
                    }
                    closeCursor()
                }
            } finally {
                _meta = null
                closed = true
            }
        }

        private fun checkClosed() {
            if (closed) {
                throw IllegalStateException("ResultSet already closed")
            }
        }
    }
}
