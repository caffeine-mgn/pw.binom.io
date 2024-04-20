package pw.binom.validate

import kotlin.jvm.JvmInline

@JvmInline
value class ValueValidateResult(val raw: String?) : ValidateResult {
  companion object {
    fun success() = ValueValidateResult(null)
    fun fail(message: String) = ValueValidateResult(message)
  }

  override val isValid
    get() = raw == null
  override val isNotValid
    get() = raw != null
  override val messageOrNull
    get() = raw

  override fun toString(): String = "ValidateResult(${if (isValid) "OK" else "\"$raw\""})"
}
