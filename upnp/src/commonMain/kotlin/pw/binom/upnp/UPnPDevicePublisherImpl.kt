package pw.binom.upnp

import kotlinx.coroutines.*
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.socket.MulticastUdpSocket
import pw.binom.io.socket.MutableInetSocketAddress
import pw.binom.io.socket.NetworkInterface
import pw.binom.io.use
import pw.binom.network.NetworkManager
import pw.binom.thread.DefaultUncaughtExceptionHandler
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import kotlin.coroutines.CoroutineContext

internal class UPnPDevicePublisherImpl(
  nm: NetworkManager,
  networkInterface: NetworkInterface,
  context: CoroutineContext,
  val source: UPnPDevicePublisher.Source,

  val uncaughtExceptionHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler,
) : UPnPDevicePublisher {

  @OptIn(DelicateCoroutinesApi::class)
  private val job = GlobalScope.launch(context) {
    try {
      val multicast = MulticastUdpSocket(
        port = 1900,
        networkInterface = networkInterface,
      )
      multicast.setTtl(UByte.MAX_VALUE)
      multicast.joinGroup(
        address = InetSocketAddress.resolve(
          host = UPnPDiscover.IP,
          port = UPnPDiscover.PORT
        ),
        netIf = networkInterface
      )
      nm.attach(multicast).use { con ->
        ByteBuffer(1536).use { buf ->
          while (isActive) {
            val source = MutableInetSocketAddress()
            buf.clear()
            val read = con.read(buf, source)
            if (read > 0) {
              buf.flip()
              val str = buf.toByteArray().decodeToString()
              if (!str.startsWith("M-SEARCH * HTTP/1.")) {
                continue
              }
              val headers = SearchRequest(UPnPUtils.parseHeaders(str))
              this@UPnPDevicePublisherImpl.source.discover(
                headers = headers,
              ) {
                buf.clear()
                buf.write(it.message)
                buf.flip()
                con.write(buf, source)
              }
            }
          }
        }
      }
    } catch (e: Throwable) {
      uncaughtExceptionHandler.uncaughtException(
        thread = Thread.currentThread,
        throwable = e,
      )
    }
  }

  override suspend fun asyncClose() {
    job.cancelAndJoin()
  }

}
