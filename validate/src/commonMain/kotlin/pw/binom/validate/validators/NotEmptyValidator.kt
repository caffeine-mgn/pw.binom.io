package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.Validator
import pw.binom.validate.ValueValidateResult
import pw.binom.validate.annotations.NotEmpty

object NotEmptyValidator : Validator.FieldValidator<NotEmpty> {
  override fun valid(annotation: NotEmpty, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
    value ?: return ValueValidateResult.success()
    if (value.isEmpty()) {
      return ValueValidateResult.fail("Value is empty")
    }
    return ValueValidateResult.success()
  }
}
