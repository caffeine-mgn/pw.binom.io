package pw.binom.upnp

import kotlin.jvm.JvmInline

@JvmInline
value class UPnPDevice internal constructor(val data: Map<String, String>) {
  companion object {
    fun create(
      st: String,
      location: String,
      server: String? = "binom-upnp-client UPnP/2.0 Server/1.0",
      other: Map<String, String> = emptyMap(),
    ): UPnPDevice {
      require(!other.keys.any { it.lowercase() == "date" }) { "Invalid header value DATE. Header can't contains dynamic variable" }
      val data = HashMap<String, String>()
      data["st"] = st
      data["location"] = location
      if (server != null) {
        data["server"] = server
      }
      return UPnPDevice(data)
    }
  }

  val st: String?
    get() = data["st"]
  val location: String?
    get() = data["location"]
}
