package pw.binom.db.postgresql.async

internal inline fun <reified T : Any> checkType(value: Any) {
    check(value is T) { "Unexpected object type. Extends ${T::class}, actual ${value::class}" }
}
