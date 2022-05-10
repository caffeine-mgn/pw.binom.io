package pw.binom.db.tarantool.protocol

import pw.binom.db.tarantool.TarantoolException

internal data class Package(val header: Map<Int, Any?>, val body: Map<Int, Any?>) {

    val code: Int
        get() = header[Key.CODE.id]?.let { it as Int } ?: 0

    val isError
        get() = code != 0

    val errorMessage
        get() = body[Key.ERROR.id] as? String?

    fun assertException() {
        if (isError) {
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
