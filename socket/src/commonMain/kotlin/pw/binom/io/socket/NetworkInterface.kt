package pw.binom.io.socket

interface NetworkInterface {
  companion object {
    fun getAvailable() = getAvailableNetworkInterfaces()
    fun findByName(name: String) = getAvailable().find { it.name == name }
    fun findByIp(ip: String) = getAvailable().find { it.ip.host == ip }
    fun getByIp(ip: String) =
      findByIp(ip) ?: throw IllegalStateException("Can't find network interface with ip \"$ip\"")

    fun findByIp(ip: NetworkAddress) = getAvailable().find { it.ip.host == ip.host }
    fun getByIp(ip: NetworkAddress) = findByIp(ip)?:throw IllegalStateException("Can't find network interface with ip \"$ip\"")
  }

  val index: Int
  val ip: InetAddress
  val prefixLength: Int
  val name: String
}

internal expect fun getAvailableNetworkInterfaces(): List<NetworkInterface>
