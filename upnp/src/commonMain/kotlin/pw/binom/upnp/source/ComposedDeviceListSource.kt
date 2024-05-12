package pw.binom.upnp.source

import pw.binom.upnp.SearchRequest
import pw.binom.upnp.UPnPDevicePublisher
import pw.binom.upnp.UPnPPublication

class ComposedDeviceListSource(vararg val sources: UPnPDevicePublisher.Source) : UPnPDevicePublisher.Source {
  override suspend fun discover(headers: SearchRequest, send: suspend (UPnPPublication) -> Unit) {
    sources.forEach {
      it.discover(headers, send)
    }
  }
}

operator fun UPnPDevicePublisher.Source.plus(other: UPnPDevicePublisher.Source): ComposedDeviceListSource =
  ComposedDeviceListSource(this, other)
