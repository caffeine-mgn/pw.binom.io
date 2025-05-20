package pw.binom.strong.properties

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.properties.IniParser
import pw.binom.properties.PropertyValue
import pw.binom.properties.serialization.PropertiesDecoder
import pw.binom.validate.Validation
import pw.binom.validate.ValidatorModule

class BaseStrongProperties(
  val serializersModule: SerializersModule = EmptySerializersModule(),
  val validatorModule: ValidatorModule = ValidatorModule.default,
  val prefix: String = "",
) : StrongProperties {
  private var properties: PropertyValue.Object = PropertyValue.Object.EMPTY

  fun add(
    key: String,
    value: String?,
  ): BaseStrongProperties = add(mapOf(key to value))

  fun add(values: Map<String, String?>): BaseStrongProperties {
    properties += IniParser.parseMap(values)
    return this
  }

  fun add(properties: PropertyValue.Object): BaseStrongProperties {
    this.properties += properties
    return this
  }

  fun addEnvironment(
    prefix: String = "strong_",
    caseSensitive: Boolean = false,
  ): BaseStrongProperties {
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
  ): BaseStrongProperties {
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
  override fun <T : Any> parse(serializer: KSerializer<T>, serializersModule: SerializersModule): T {
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
}
