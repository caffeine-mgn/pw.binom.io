package pw.binom.strong.serialization

import kotlinx.serialization.KSerializer
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.injectServiceList

class SerializationService {
  private val providers by injectServiceList<SerializationProvider>()
  private val types by BeanLifeCycle.afterInit {
    val map = HashMap<String, SerializationProvider>()
    providers.forEach { provider ->
      provider.mimeTypes.forEach { mime ->
        map[mime] = provider
      }
    }
    map
  }

  fun findProvider(mimeType: String) = types[mimeType]
  fun getProvider(mimeType: String) = findProvider(mimeType) ?: throw ProviderNotFoundException(mimeType)

  fun <T> encode(mimeType: String, serializer: KSerializer<T>, value: T) =
    getProvider(mimeType).encode(serializer, value)

  fun <T> decode(mimeType: String, serializer: KSerializer<T>, data: ByteArray): T =
    getProvider(mimeType).decode(serializer, data)
}
