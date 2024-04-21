package pw.binom.validate

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

object Validation {

  @OptIn(InternalSerializationApi::class)
  inline fun <reified T : Any> validateAndCheck(
    value: T,
    validatorModule: ValidatorModule = ValidatorModule.default,
    serializersModule: SerializersModule = EmptySerializersModule(),
  ) = validateAndCheck(
    strategy = T::class.serializer(),
    value = value,
    validatorModule = validatorModule,
    serializersModule = serializersModule,
  )

  fun <T : Any> validateAndCheck(
    strategy: SerializationStrategy<T>,
    value: T,
    validatorModule: ValidatorModule = ValidatorModule.default,
    serializersModule: SerializersModule = EmptySerializersModule(),
  ) {
    val collector = ErrorCollector.default
    validate(
      strategy = strategy,
      value = value,
      validatorModule = validatorModule,
      serializersModule = serializersModule,
      errorCollector = collector,
    )
    collector.flushAndCheck()
  }

  fun <T : Any> validate(
    strategy: SerializationStrategy<T>,
    value: T,
    errorCollector: ErrorCollector,
    validatorModule: ValidatorModule,
    serializersModule: SerializersModule = EmptySerializersModule(),
  ) {

    val encoder = ObjectValidatorEncoder(
      prefix = Prefix.EMPTY,
      collector = errorCollector,
      validators = emptyMap(),
      serializersModule = serializersModule,
      validatorModule = validatorModule,
      validators2 = emptyList()
    )
    strategy.serialize(encoder = encoder, value = value)
  }
}
