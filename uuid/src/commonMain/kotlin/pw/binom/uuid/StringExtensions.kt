package pw.binom.uuid

@Suppress("NOTHING_TO_INLINE")
inline fun String.toUUID() = UUID.fromString(this)

fun String.toUUIDOrNull() = try {
    UUID.fromString(this)
} catch (e: Throwable) {
    null
}
