package pw.binom.validate.validators

import kotlinx.serialization.descriptors.SerialDescriptor
import pw.binom.validate.ErrorCollector
import pw.binom.validate.Validator
import pw.binom.validate.ValidatorModule
import pw.binom.validate.annotations.OneOf

object OneOfValidator : Validator.ObjectValidator<OneOf> {
  override fun startObject(
    annotation: OneOf,
    field: String,
    descriptor: SerialDescriptor,
    validatorModule: ValidatorModule,
    errorCollector: ErrorCollector,
  ): Validator.ObjectValidator.CommonValidator {
    if (annotation.fields.isEmpty()) {
      return Validator.ObjectValidator.CommonValidator.EMPTY
    }
    return InternalValidator(
      field = field,
      fields = annotation.fields.toSet(),
      errorCollector = errorCollector,
    )
  }

  private class InternalValidator(
    val field: String,
    val fields: Set<String>,
    val errorCollector: ErrorCollector,
  ) :
    Validator.ObjectValidator.CommonValidator {
    private val notNull = HashSet<String>()
    override fun valid(fieldName: String, value: String?) {
      if (fieldName !in fields) {
        return
      }
      if (value != null) {
        notNull += fieldName
      }
    }

    override fun finish() {
      if (notNull.size > 1) {
        errorCollector.invalidField(
          fieldName = field,
          message = "Only one field of ${fields.joinToString()} should be not null. Not null fields: ${notNull.joinToString()}"
        )
        return
      }
      if (notNull.isEmpty()) {
        errorCollector.invalidField(
          fieldName = field,
          message = "One field of ${fields.joinToString()} should be not null"
        )
      }
    }

  }
}
