package pw.binom.db.serialization

fun interface DateContainer {
  operator fun set(
    key: String,
    value: Any?,
    useQuotes: Boolean,
  )

  companion object {
    val EMPTY = DateContainer { key, value, useQuotes -> throw IllegalStateException("Not supported") }
  }
}
