package pw.binom.upnp

import kotlinx.coroutines.*
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.socket.NetworkInterface
import pw.binom.io.socket.UdpNetSocket
import pw.binom.io.use
import pw.binom.io.wrap
import pw.binom.network.NetworkManager
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object UPnPDiscover {
  /**
   * The SSDP port
   */
  const val PORT: Int = 1900

  /**
   * The broadcast address to use when trying to contact UPnP devices
   */
  const val IP = "239.255.255.250"

  /**
   * The default timeout for the initial broadcast request
   */
  const val DEFAULT_TIMEOUT = 3000

  /**
   * The default gateway types to use in search
   */
  val DEFAULT_SEARCH_TYPES = arrayOf(
    "urn:schemas-upnp-org:device:InternetGatewayDevice:1",
    "urn:schemas-upnp-org:service:WANIPConnection:1",
    "urn:schemas-upnp-org:service:WANPPPConnection:1"
  )

  @OptIn(DelicateCoroutinesApi::class)
  suspend fun discover(
    nm: NetworkManager,
    searchTypes: Collection<String> = DEFAULT_SEARCH_TYPES.toList(),
    timeout: Duration = 3.seconds,
  ) = NetworkInterface.getAvailable().map { netIf ->
    GlobalScope.async(coroutineContext) {
      netIf to discover(
        nm = nm,
        netIf = netIf,
        searchTypes = searchTypes,
        timeout = timeout,
      )
    }
  }.awaitAll().toMap()

  @OptIn(DelicateCoroutinesApi::class)
  suspend fun discover(
    nm: NetworkManager,
    netIf: NetworkInterface,
    searchTypes: Collection<String> = DEFAULT_SEARCH_TYPES.toList(),
    timeout: Duration = 10.seconds,
  ): List<UPnPDevice> {
    val addrForSend = InetSocketAddress.resolve(host = IP, port = PORT)
    val results = searchTypes.map { searchType ->
      GlobalScope.async(coroutineContext + nm) {
        try {
          val searchMessage = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST: $IP:$PORT\r\n" +
            "ST: $searchType\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: 2\r\n" +    // seconds to delay response
            "\r\n"
          val result = ArrayList<UPnPDevice>()
          nm.attach(UdpNetSocket()).use { con ->
            con.bind(netIf.ip.withPort(0))
            try {
              searchMessage.encodeToByteArray().wrap { buf ->
                con.write(buf, addrForSend)
              }
            } catch (e: Throwable) {
              return@async emptyList<UPnPDevice>()
            }
            while (true) {
              val responseText = ByteBuffer(1536).use { buf ->
                withTimeoutOrNull(timeout) {
                  val r = con.read(buf, null)
                  if (r > 0) {
                    buf.flip()
                    buf.toByteArray().decodeToString()
                  } else {
                    null
                  }
                }
              }
              responseText ?: break
              println("Response got:\n$responseText")
              if (responseText.startsWith("M-SEARCH *")) {
                continue
              }
              val headers = UPnPUtils.parseHeaders(responseText)
              if (headers.isEmpty()) {
                continue
              }
              result += UPnPDevice(headers)
            }
          }
          result
        } catch (e: Throwable) {
          e.printStackTrace()
          throw e
        }
      }
    }.awaitAll()
    return results.asSequence().flatten().distinctBy { "${it.st}:${it.location}" }.toList()
  }
}
