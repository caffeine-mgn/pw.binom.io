package pw.binom.upnp

import kotlin.jvm.JvmInline

@JvmInline
value class SearchRequest(val raw: Map<String, String>) {
  val st
    get() = raw["st"]

  operator fun get(key: String) = raw[key]
}
