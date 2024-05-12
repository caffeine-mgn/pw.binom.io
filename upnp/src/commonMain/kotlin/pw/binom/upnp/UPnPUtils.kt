package pw.binom.upnp

internal object UPnPUtils {
  fun parseHeaders(text: String) =
    text.lineSequence()
      .mapNotNull {
        val items = it.split(':', limit = 2)
        if (items.size != 2) {
          return@mapNotNull null
        }
        items[0].lowercase() to items[1]
      }
      .toMap()
}
