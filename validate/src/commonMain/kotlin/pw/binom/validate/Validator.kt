package pw.binom.validate

import kotlinx.serialization.descriptors.SerialDescriptor

sealed interface Validator<T : Annotation> {
  interface FieldValidator<T : Annotation> : Validator<T> {
    fun valid(annotation: T, descriptor: SerialDescriptor, value: String?): ValueValidateResult
  }

  interface ObjectValidator<T : Annotation> : Validator<T> {
    fun startObject(annotation: T, field:String, descriptor: SerialDescriptor, errorCollector: ErrorCollector): CommonValidator

    interface CommonValidator {
      companion object {
        val EMPTY = object : CommonValidator {
          override fun valid(fieldName: String, value: String?) {
            // Do nothing
          }

          override fun finish() {
            // Do nothing
          }

        }
      }

      fun valid(fieldName: String, value: String?)
      fun finish()
    }
  }
}
