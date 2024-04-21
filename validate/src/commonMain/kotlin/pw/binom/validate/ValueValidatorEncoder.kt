package pw.binom.validate

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

internal class ValueValidatorEncoder(
  val prefix: Prefix,
  val collector: ErrorCollector,
  val validatorModule: ValidatorModule,
  override val serializersModule: SerializersModule,
  val objectValidators: List<Validator.ObjectValidator.CommonValidator>,
) : CompositeEncoder {

  private fun checkValid(descriptor: SerialDescriptor, index: Int, value: String?) {
    val d = descriptor.getElementDescriptor(index)
    descriptor.getElementAnnotations(index).forEach {
      val validator = validatorModule.findValueValidator(it) ?: return@forEach
      val message = validator.valid(
        annotation = it,
        descriptor = d,
        validatorModule = validatorModule,
        value = value,
      ).messageOrNull
      if (message != null) {
        collector.invalidField(
          fieldName = (prefix + descriptor.getElementName(index)).toString(),
          message = message,
        )
      }
    }
    objectValidators.forEach {
      it.valid(
        fieldName = descriptor.getElementName(index), value = value
      )
    }
  }

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = ObjectValidatorEncoder(
    prefix = prefix + descriptor.getElementName(index),
    collector = collector,
    validatorModule = validatorModule,
    serializersModule = serializersModule,
    validators = descriptor.getElementAnnotations(index).asSequence().mapNotNull {
      it to (validatorModule.findValueValidator(it) ?: return@mapNotNull null)
    }.toMap(),
    validators2 = emptyList(),
  )

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?,
  ) {
    if (value == null) {
      checkValid(
        descriptor = descriptor, index = index, value = null
      )
    } else {
      val vv = if (descriptor.getElementDescriptor(index).kind is PrimitiveKind) {
        objectValidators
      } else {
        emptyList()
      }
      checkValid(
        descriptor = descriptor, index = index, value = descriptor.getElementDescriptor(index).serialName
      )
      val encoder = ObjectValidatorEncoder(
        prefix = prefix + descriptor.getElementName(index),
        collector = collector,
        validators = descriptor.getElementAnnotations(index).asSequence().mapNotNull {
          it to (validatorModule.findValueValidator(it) ?: return@mapNotNull null)
        }.toMap(),
        validatorModule = validatorModule,
        serializersModule = serializersModule,
        validators2 = vv,
      )
      serializer.serialize(encoder, value)
    }
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T,
  ) {
    encodeNullableSerializableElement(
      descriptor = descriptor,
      index = index,
      serializer = serializer,
      value = value
    )
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    checkValid(
      descriptor = descriptor, index = index, value = value.toString()
    )
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    checkValid(
      descriptor = descriptor, index = index, value = value
    )
  }

  override fun endStructure(descriptor: SerialDescriptor) {
    objectValidators.forEach {
      it.finish()
    }
  }
}
