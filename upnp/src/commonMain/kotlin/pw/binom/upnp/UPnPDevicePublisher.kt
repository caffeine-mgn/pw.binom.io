package pw.binom.upnp

import pw.binom.io.AsyncCloseable
import pw.binom.io.socket.NetworkInterface
import pw.binom.network.NetworkManager
import pw.binom.upnp.UPnPDevicePublisher.Source
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface UPnPDevicePublisher : AsyncCloseable {

  companion object {
    fun create(
      networkManager: NetworkManager,
      networkInterface: NetworkInterface,
      source: UPnPDevicePublisher.Source,
      context: CoroutineContext = EmptyCoroutineContext,
    ): UPnPDevicePublisher = UPnPDevicePublisherImpl(
      nm = networkManager,
      networkInterface = networkInterface,
      context = context,
      source = source,
    )

    fun create(
      networkManager: NetworkManager,
      source: UPnPDevicePublisher.Source,
      context: CoroutineContext = EmptyCoroutineContext,
      checkInterval: Duration = 1.minutes,
    ) = AllInterfaceUPnPDevicePublisher(
      networkManager = networkManager,
      context = context,
      checkInterval = checkInterval,
      source = source,
    )
  }

  fun interface Source {
    companion object {
      val EMPTY = Source { _, _ -> }
    }

    suspend fun discover(headers: SearchRequest, send: suspend (UPnPPublication) -> Unit)
  }
}
