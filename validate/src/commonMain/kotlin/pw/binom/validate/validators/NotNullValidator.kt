package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.Validator
import pw.binom.validate.ValueValidateResult
import pw.binom.validate.annotations.NotNull

object NotNullValidator : Validator.FieldValidator<NotNull> {
  override fun valid(annotation: NotNull, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
    value ?: return ValueValidateResult.fail("Value is null")
    return ValueValidateResult.success()
  }
}
