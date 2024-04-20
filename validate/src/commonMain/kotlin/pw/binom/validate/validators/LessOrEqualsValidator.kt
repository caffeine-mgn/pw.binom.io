package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.Validator
import pw.binom.validate.ValueValidateResult
import pw.binom.validate.annotations.LessOrEquals

object LessOrEqualsValidator : Validator.FieldValidator<LessOrEquals> {
  override fun valid(annotation: LessOrEquals, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
    value ?: return ValueValidateResult.success()
    val valueNumber =
      value.toDoubleOrNull() ?: return ValueValidateResult.fail("Can't parse field value \"$value\" to number")
    val annotationValue = annotation.value.toDoubleOrNull()
      ?: return ValueValidateResult.fail("Can't parse validation \"${annotation.value}\" to number")
    if (valueNumber <= annotationValue) {
      return ValueValidateResult.success()
    }
    return ValueValidateResult.fail("Value $valueNumber should be less or equals than $annotationValue")
  }
}
