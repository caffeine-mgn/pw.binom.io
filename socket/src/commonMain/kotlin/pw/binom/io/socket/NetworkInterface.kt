package pw.binom.io.socket

interface NetworkInterface {
  companion object {
    fun getAvailable() = getAvailableNetworkInterfaces()
  }

  val ip: String
  val prefixLength: Int
  val name: String
}

internal expect fun getAvailableNetworkInterfaces(): List<NetworkInterface>
