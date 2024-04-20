package pw.binom.validate

import pw.binom.validate.annotations.*
import pw.binom.validate.validators.*

private val emptyValidatorModule = ValidatorModule {
  define(GreaterOrEquals::class, GreaterOrEqualsValidator)
  define(Greater::class, GreaterValidator)
  define(Less::class, LessValidator)
  define(LessOrEquals::class, LessOrEqualsValidator)
  define(NotNull::class, NotNullValidator)
  define(NotBlank::class, NotBlankValidator)
  define(NotEmpty::class, NotEmptyValidator)
  define(RegExpMatch::class, RegExpMatchValidator)
  define(OneOf::class, OneOfValidator)
}

interface ValidatorModule {
  companion object {
    val default = emptyValidatorModule
  }

  fun <T : Annotation> findValueValidator(annotation: T): Validator.FieldValidator<T>?
  fun <T : Annotation> findObjectValidator(annotation: T): Validator.ObjectValidator<T>?
}

fun ValidatorModule.plus(other: ValidatorModule) = ValidatorModule {
  include(this@plus)
  include(other)
}
