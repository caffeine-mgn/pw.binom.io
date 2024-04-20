package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.Validator
import pw.binom.validate.ValueValidateResult
import pw.binom.validate.annotations.RegExpMatch

object RegExpMatchValidator : Validator.FieldValidator<RegExpMatch> {
  override fun valid(annotation: RegExpMatch, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
    value ?: return ValueValidateResult.success()
    annotation.regexp.forEach {
      if (it.toRegex().matches(value)) {
        return ValueValidateResult.success()
      }
    }
    return ValueValidateResult.fail("Value \"$value\" is not match to regexp")
  }
}
