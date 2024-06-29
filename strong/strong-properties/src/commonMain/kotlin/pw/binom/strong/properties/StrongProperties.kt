package pw.binom.strong.properties

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.properties.IniParser
import pw.binom.properties.PropertyValue
import pw.binom.properties.serialization.PropertiesDecoder
import pw.binom.validate.Validation
import pw.binom.validate.ValidatorModule

class StrongProperties(
  val serializersModule: SerializersModule = EmptySerializersModule(),
  val validatorModule: ValidatorModule = ValidatorModule.default,
  val prefix: String = "",
) {
  private var properties: PropertyValue.Object = PropertyValue.Object.EMPTY

  fun add(
    key: String,
    value: String?,
  ): StrongProperties = add(mapOf(key to value))

  fun add(values: Map<String, String?>): StrongProperties {
    properties += IniParser.parseMap(values)
    return this
  }

  fun addEnvironment(
    prefix: String = "strong_",
    caseSensitive: Boolean = false,
  ): StrongProperties {
    properties +=
      ArgumentProperties.parseEnvironment(
        prefix = prefix,
        caseSensitive = caseSensitive,
      )
    return this
  }

  fun addArgs(
    args: Array<String>,
    prefix: String = "-D",
    caseSensitive: Boolean = false,
  ): StrongProperties {
    properties +=
      ArgumentProperties.parseArguments(
        args = args,
        prefix = prefix,
        caseSensitive = caseSensitive,
      )
    return this
  }

  private val cache = mutableMapOf<DeserializationStrategy<Any>, Any>()

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> parse(serializer: KSerializer<T>, serializersModule: SerializersModule = EmptySerializersModule()): T {
    return cache.getOrPut(serializer) {
      val value = serializer.deserialize(
        PropertiesDecoder(
          root = properties,
          serializersModule = serializersModule,
          prefix = prefix,
        ),
      )
      Validation.validateAndCheck(
        strategy = serializer,
        value = value,
        validatorModule = validatorModule,
        serializersModule = serializersModule,
      )
      value
    } as T
  }

  override fun toString(): String = properties.toString()

  @OptIn(InternalSerializationApi::class)
  inline fun <reified T : Any> parse(
    serializersModule: SerializersModule = EmptySerializersModule(),
  ) = parse(
    serializer = T::class.serializer(),
    serializersModule = serializersModule,
  )
}
