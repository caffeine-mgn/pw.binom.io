package pw.binom.upnp.source

import pw.binom.upnp.*

class DeviceFilteredListSource(list: List<UPnPDevice>) : UPnPDevicePublisher.Source {

  private val stFilters: Map<String?, List<DefaultUPnPPublication>>

  init {
    val withFilterMap = HashMap<String?, ArrayList<DefaultUPnPPublication>>()
    list.forEach {
      withFilterMap.getOrPut(it.st) { ArrayList() }.add(DefaultUPnPPublication(it))
    }
    stFilters = withFilterMap
  }

  override suspend fun discover(headers: SearchRequest, send: suspend (UPnPPublication) -> Unit) {
    stFilters[headers.st]?.forEach {
      send(it)
    }
  }
}
