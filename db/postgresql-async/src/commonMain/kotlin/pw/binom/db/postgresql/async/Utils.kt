package pw.binom.db.postgresql.async

import pw.binom.db.TransactionMode

internal inline fun <reified T : Any> checkType(value: Any) {
    check(value is T) { "Unexpected object type. Extends ${T::class}, actual ${value::class}" }
}

internal val TransactionMode.pg
    get() = when (this) {
        TransactionMode.SERIALIZABLE -> "SERIALIZABLE"
        TransactionMode.READ_COMMITTED -> "READ COMMITTED"
        TransactionMode.REPEATABLE_READ -> "REPEATABLE READ"
        TransactionMode.READ_UNCOMMITTED -> "READ UNCOMMITTED"
    }
