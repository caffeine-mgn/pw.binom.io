package pw.binom.db.tarantool.protocol

import pw.binom.db.tarantool.TarantoolException

internal data class Package(val header: Map<Int, Any?>, val body: Map<Int, Any?>) {

    fun assertException() {
        val code = header[Key.CODE.id] ?: return
        if (code != 0) {
            val message = body[Key.ERROR.id] as String?
            throw if (message == null)
                TarantoolException()
            else
                TarantoolException(message)
        }
    }

    val data
        get() = body[Key.DATA.id]
}