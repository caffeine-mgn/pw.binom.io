package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.Validator
import pw.binom.validate.ValueValidateResult
import pw.binom.validate.annotations.NotBlank

object NotBlankValidator : Validator.FieldValidator<NotBlank> {
  override fun valid(annotation: NotBlank, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
    value ?: return ValueValidateResult.success()
    if (value.isBlank()) {
      return ValueValidateResult.fail("Value is blank")
    }
    return ValueValidateResult.success()
  }
}
