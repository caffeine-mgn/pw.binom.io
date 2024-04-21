package pw.binom.validate

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

internal class ObjectValidatorEncoder(
  val prefix: Prefix,
  val collector: ErrorCollector,
  val validatorModule: ValidatorModule,
  override val serializersModule: SerializersModule,
  val validators: Map<out Annotation, Validator.FieldValidator<in Annotation>>,
  val validators2: Collection<Validator.ObjectValidator.CommonValidator>,
) : Encoder {
  companion object {
    private val NULL_DESCRIPTOR = PrimitiveSerialDescriptor("null", PrimitiveKind.INT)
  }

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
    ValueValidatorEncoder(
      prefix = prefix,
      collector = collector,
      validatorModule = validatorModule,
      serializersModule = serializersModule,
      objectValidators = descriptor.annotations.mapNotNull {
        validatorModule.findObjectValidator(it)?.startObject(
          field = prefix.toString(),
          annotation = it,
          descriptor = descriptor,
          errorCollector = collector,
          validatorModule = validatorModule,
        )
      }
    )

  private fun check(descriptor: SerialDescriptor, value: String?) {
    validators.forEach { (annotation, validator) ->
      val message = validator.valid(
        annotation,
        descriptor = descriptor,
        value = value,
        validatorModule = validatorModule,
      ).messageOrNull
      if (message != null) {
        collector.invalidField(
          fieldName = prefix.toString(),
          message = message
        )
      }
    }
    validators2.forEach {
      it.valid(
        fieldName = prefix.toString(),
        value = value,
      )
    }
  }

  override fun encodeBoolean(value: Boolean) {
    check(
      descriptor = Boolean.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeByte(value: Byte) {
    check(
      descriptor = Byte.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeChar(value: Char) {
    check(
      descriptor = Char.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeDouble(value: Double) {
    check(
      descriptor = Double.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    check(
      descriptor = Boolean.serializer().descriptor,
      value = enumDescriptor.getElementName(index),
    )
  }

  override fun encodeFloat(value: Float) {
    check(
      descriptor = Float.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

  override fun encodeInt(value: Int) {
    check(
      descriptor = Int.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeLong(value: Long) {
    check(
      descriptor = Long.serializer().descriptor,
      value = value.toString(),
    )
  }

  @ExperimentalSerializationApi
  override fun encodeNull() {
    check(
      descriptor = NULL_DESCRIPTOR,
      value = null,
    )
  }

  override fun encodeShort(value: Short) {
    check(
      descriptor = Short.serializer().descriptor,
      value = value.toString(),
    )
  }

  override fun encodeString(value: String) {
    check(
      descriptor = String.serializer().descriptor,
      value = value,
    )
  }
}
