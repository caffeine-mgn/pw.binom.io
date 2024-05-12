package pw.binom.upnp.source

import pw.binom.upnp.*

class DeviceListSource(list: Collection<UPnPDevice>) : UPnPDevicePublisher.Source {

  private val list = list.map { DefaultUPnPPublication(it) }

  override suspend fun discover(headers: SearchRequest, send: suspend (UPnPPublication) -> Unit) {
    list.forEach {
      send(it)
    }
  }
}
