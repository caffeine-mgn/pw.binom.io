package pw.binom.strong.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.*
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.injectServiceList

class SerializationService {
  private val providers by injectServiceList<SerializationProvider>()
  private val modules by injectServiceList<SerializationModuleExtender>()
  private val module by BeanLifeCycle.afterInit {
    modules.fold(EmptySerializersModule()) { acc, value ->
      value.extends(acc)
    }
  }
  private val types by BeanLifeCycle.afterInit {
    val map = HashMap<String, SerializationProvider>()
    providers.forEach { provider ->
      provider.mimeTypes.forEach { mime ->
        provider.init(module)
        map[mime] = provider
      }
    }
    map
  }

  val availableTypes: Set<String>
    get() = types.keys

  fun findProvider(mimeType: String) = types[mimeType]
  fun findProvider(mimeTypes: List<String>): SerializationProvider? {
    mimeTypes.forEach { mimeType ->
      val exist = types[mimeType]
      if (exist != null) {
        return exist
      }
    }
    return null
  }

  fun getProvider(mimeType: String) = findProvider(mimeType) ?: throw ProviderNotFoundException(mimeType)
  fun getProvider(mimeType: List<String>) =
    findProvider(mimeType) ?: throw ProviderNotFoundException(mimeType.joinToString())

  fun <T> encode(mimeType: String, serializer: KSerializer<T>, value: T) =
    getProvider(mimeType).encode(serializer, value)

  fun <T> decode(mimeType: String, serializer: KSerializer<T>, data: ByteArray): T =
    getProvider(mimeType).decode(serializer, data)


  fun <T> encode(mimeTypes: List<String>, serializer: KSerializer<T>, value: T) =
    getProvider(mimeTypes).encode(serializer, value)

  fun <T> decode(mimeTypes: List<String>, serializer: KSerializer<T>, data: ByteArray): T =
    getProvider(mimeTypes).decode(serializer, data)
}
