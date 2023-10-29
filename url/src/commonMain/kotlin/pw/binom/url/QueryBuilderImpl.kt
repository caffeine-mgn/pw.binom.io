package pw.binom.url

internal class QueryBuilderImpl(val sb: Appendable) : QueryBuilder {
  private var first = true
  private fun prepare() {
    if (!first) {
      sb.append("&")
    }
    first = false
  }

  private fun keyCheck(key: String) {
    if (key.isEmpty()) {
      throw IllegalArgumentException("Key can't be empty")
    }
  }

  override fun add(key: String, value: String?) {
    keyCheck(key)
    prepare()

    sb.append(UrlEncoder.encode(key))
    if (value != null) {
      sb.append("=").append(UrlEncoder.encode(value))
    }
  }

  override fun add(key: String) {
    add(key = key, value = null)
  }
}
